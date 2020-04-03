package org.akhq.rest;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import lombok.extern.slf4j.Slf4j;
import org.akhq.models.AccessControlList;
import org.akhq.repositories.RecordRepository;
import org.akhq.service.AclsService;
import org.akhq.service.TopicService;
import org.akhq.service.dto.acls.AclsDTO;
import org.akhq.service.dto.consumerGroup.ConsumerGroupDTO;
import org.akhq.service.dto.topic.ConfigOperationDTO;
import org.akhq.service.dto.topic.ConfigDTO;
import org.akhq.service.dto.topic.*;
import org.apache.kafka.common.resource.ResourceType;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller("${akhq.server.base-path:}/api")
public class TopicResource {
    private TopicService topicService;
    private AclsService aclsService;

    @Inject
    public TopicResource(TopicService topicService, AclsService aclsService) {
        this.topicService = topicService;
        this.aclsService = aclsService;
    }

    @Get("/topics")
    public TopicListDTO fetchAllTopics(String clusterId, String view, @Nullable String search, Optional<Integer> pageNumber) throws ExecutionException, InterruptedException {
        log.debug("Fetch all topics by name");
        return topicService.getTopics(clusterId, view, search, pageNumber);
    }

    @Post("/topic/create")
    public void topicCreate(@Body CreateTopicDTO createTopicDTO) throws ExecutionException, InterruptedException {
        log.debug("Create topic {}", createTopicDTO.getTopicId());
        topicService.createTopic(createTopicDTO);
    }

    @Post("/topic/produce")
    public void topicProduce(@Body ProduceTopicDTO produceTopicDTO) throws ExecutionException, InterruptedException {
        log.debug("Producing to topic {}, message: {}", produceTopicDTO.getTopicId(), produceTopicDTO.getValue());
        topicService.produceToTopic(produceTopicDTO);
    }

    @Get("/topic/data")
    public TopicDataDTO fetchTopicData(String clusterId, String topicId,
                                       Optional<RecordRepository.Options.Sort> sort,
                                       Optional<Integer> partition,
                                       Optional<String> timestamp,
                                       Optional<String> search,
                                       Optional<String> after,
                                       Optional<Integer> pageNumber) throws ExecutionException, InterruptedException {
        log.debug("Fetch data from topic: {}", topicId);
        return topicService.getTopicData(clusterId, topicId, after, partition, sort, timestamp, search);
    }

    @Get("/topic/partitions")
    public List<PartitionDTO> fetchTopicPartitions(String clusterId, String topicId) throws ExecutionException, InterruptedException {
        log.debug("Fetch partitions from topic: {}", topicId);
        return topicService.getTopicPartitions(clusterId, topicId);
    }

    @Get("/topic/logs")
    public List<LogDTO> fetchTopicLogs(String clusterId, String topicId) throws ExecutionException, InterruptedException {
        log.debug("Fetch logs from topic: {}", topicId);
        return topicService.getTopicLogs(clusterId, topicId);
    }

    @Delete("/topic/delete")
    public TopicListDTO deleteTopic(@Body DeleteTopicDTO deleteTopicDTO) throws ExecutionException, InterruptedException {
        log.debug("Delete topic: {}", deleteTopicDTO.getTopicId());
        topicService.deleteTopic(deleteTopicDTO.getClusterId(), deleteTopicDTO.getTopicId());
        return topicService.getTopics(deleteTopicDTO.getClusterId(), "ALL", "", Optional.empty());
    }

    @Get("/topic/groups")
    public List<ConsumerGroupDTO> fetchTopicGroups(String clusterId, String topicId) throws ExecutionException, InterruptedException {
        log.debug("Fetch topic groups ");
        return topicService.getConsumerGroups(clusterId, topicId);
    }

    @Get("/topic/acls")
    public List<List<AclsDTO>> fetchTopicAcl(String clusterId, String topicId) throws ExecutionException, InterruptedException {
        log.debug("Fetch topic acls ");
        return aclsService.getAcls(clusterId, ResourceType.TOPIC, topicId);
    }

    @Get("/cluster/topic/configs")
    public List<ConfigDTO> fetchTopicConfigs(String clusterId, String topicId) throws ExecutionException, InterruptedException {
        log.debug("Fetch node {} configs from cluster: {}", topicId, clusterId);
        return topicService.getConfigDTOList(clusterId, topicId);
    }
    @Post("cluster/topic/update-configs")
    public List<org.akhq.service.dto.topic.ConfigDTO> updateNodeConfigs(@Body ConfigOperationDTO configOperation) throws Throwable {
        log.debug("Update node {} configs from cluster: {}", configOperation.getTopicId(), configOperation.getClusterId());
        return topicService.updateConfigs(configOperation);
    }
}

