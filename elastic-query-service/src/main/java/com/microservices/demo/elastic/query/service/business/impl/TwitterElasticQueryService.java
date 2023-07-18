package com.microservices.demo.elastic.query.service.business.impl;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.service.QueryType;
import com.microservices.demo.elastic.query.service.business.ElasticQueryService;
import com.microservices.demo.elastic.query.service.common.exception.ElasticQueryServiceException;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceAnalyticsResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceWordCountResponseModel;
import com.microservices.demo.elastic.query.service.model.assembler.ElasticQueryServiceResponseModelAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TwitterElasticQueryService implements ElasticQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticQueryService.class);

    private final ElasticQueryServiceResponseModelAssembler assembler;

    private final ElasticQueryClient<TwitterIndexModel> elasticQueryClient;

    private final ElasticQueryServiceConfigData elasticQueryServiceConfigData;

    private final WebClient.Builder webClientBuilder;

    public TwitterElasticQueryService(ElasticQueryServiceResponseModelAssembler assembler,
                                      ElasticQueryClient<TwitterIndexModel> queryClient,
                                      ElasticQueryServiceConfigData queryServiceConfigData,
                                      @Qualifier("webClientBuilder")
                                      WebClient.Builder clientBuilder) {
        this.assembler = assembler;
        this.elasticQueryClient = queryClient;
        this.elasticQueryServiceConfigData = queryServiceConfigData;
        this.webClientBuilder = clientBuilder;
    }

    @Override
    public ElasticQueryServiceResponseModel getDocumentById(String id) {
        LOG.info("Querying elasticsearch by id {}", id);
        return assembler.toModel(elasticQueryClient.getIndexModelById(id));
    }

    @Override
    public ElasticQueryServiceAnalyticsResponseModel getDocumentByText(String text, String accessToken) {
        LOG.info("Querying elasticsearch by text {}", text);
        List<ElasticQueryServiceResponseModel> elasticQueryServiceResponseModels = assembler.toModels(elasticQueryClient.getIndexModelByText(text));
        return ElasticQueryServiceAnalyticsResponseModel.builder()
                .queryResponseModels(elasticQueryServiceResponseModels)
                .wordCount(getWordCount(text, accessToken))
                .build();
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getAllDocuments() {
        LOG.info("Querying all documents in elasticsearch");
        return assembler.toModels(elasticQueryClient.getAllIndexModels());
    }

    private Long getWordCount(String text, String accessToken) {
        if (QueryType.KAFKA_STATE_STORE.getType().equals(elasticQueryServiceConfigData.getWebClient().getQueryType())) {
            return getFromKafkaStateStore(text, accessToken).getWordCount();
        }
        return 0L;
    }

    private ElasticQueryServiceWordCountResponseModel getFromKafkaStateStore(String text, String accessToken) {
        ElasticQueryServiceConfigData.Query queryFromKafkaStateStore = elasticQueryServiceConfigData.getQueryFromKafkaStateStore();
        return retrieveResponseModel(text, accessToken, queryFromKafkaStateStore);
    }

    private ElasticQueryServiceWordCountResponseModel retrieveResponseModel(String text, String accessToken, ElasticQueryServiceConfigData.Query query) {
        return webClientBuilder
                .build()
                .method(HttpMethod.valueOf(query.getMethod()))
                .uri(query.getUri(), uriBuilder -> uriBuilder.build(text))
                .headers(h -> h.setBearerAuth(accessToken))
                .accept(MediaType.valueOf(query.getAccept()))
                .retrieve()
                .onStatus(s -> s.equals(HttpStatus.UNAUTHORIZED), clientResponse -> Mono.just(new BadCredentialsException("Not authenticated")))
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.just(new ElasticQueryServiceException(clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.just(new Exception(clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(ElasticQueryServiceWordCountResponseModel.class)
                .log()
                .block();
    }
}
