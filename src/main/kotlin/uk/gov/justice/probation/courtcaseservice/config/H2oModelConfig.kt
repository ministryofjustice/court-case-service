package uk.gov.justice.probation.courtcaseservice.config

import hex.genmodel.MojoModel
import hex.genmodel.MojoReaderBackendFactory
import hex.genmodel.easy.EasyPredictModelWrapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class H2oModelConfig(@Value("\${h2o.model-path}") private val h2oResource: Resource) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  fun getModelWrapper(): EasyPredictModelWrapper {
    log.info("Creating H2O Model runtime from zip file")
    return EasyPredictModelWrapper(MojoModel.load(MojoReaderBackendFactory.createReaderBackend(h2oResource.inputStream, MojoReaderBackendFactory.CachingStrategy.MEMORY)))
  }
}
