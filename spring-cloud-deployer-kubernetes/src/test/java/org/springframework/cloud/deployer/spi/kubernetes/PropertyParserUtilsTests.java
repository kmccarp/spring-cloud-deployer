/*
 * Copyright 2018-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.deployer.spi.kubernetes;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.deployer.spi.kubernetes.support.PropertyParserUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for PropertyParserUtils
 *
 * @author Chris Schaefer
 * @author Ilayaperumal Gopinathan
 * @author Glenn Renfro
 */
public class PropertyParserUtilsTests {

	@Test
	public void testAnnotationParseSingle() {
		Map<String, String> annotations = PropertyParserUtils.getStringPairsToMap("annotation:value");
		assertThat(annotations.isEmpty()).isFalse();
		assertThat(annotations.size() == 1).isTrue();
		assertThat(annotations.containsKey("annotation")).isTrue();
		assertThat("value".equals(annotations.get("annotation"))).isTrue();
	}

	@Test
	public void testAnnotationParseMultiple() {
		Map<String, String> annotations = PropertyParserUtils.getStringPairsToMap("annotation1:value1,annotation2:value2");
		assertThat(annotations.isEmpty()).isFalse();
		assertThat(annotations.size() == 2).isTrue();
		assertThat(annotations.containsKey("annotation1")).isTrue();
		assertThat("value1".equals(annotations.get("annotation1"))).isTrue();
		assertThat(annotations.containsKey("annotation2")).isTrue();
		assertThat("value2".equals(annotations.get("annotation2"))).isTrue();
	}

	@Test
	public void testAnnotationParseMultipleWithCommas() {
		assertThat(PropertyParserUtils.getStringPairsToMap("annotation1:\"value1,a,b,c,d\",annotation2:value2"))
				.isNotEmpty()
				.hasSize(2)
				.containsEntry("annotation1", "\"value1,a,b,c,d\"")
				.containsEntry("annotation2", "value2");
		assertThat(PropertyParserUtils.getStringPairsToMap("annotation1:value1,annotation2:\"value2,a,b,c,d\""))
				.isNotEmpty()
				.hasSize(2)
				.containsEntry("annotation1", "value1")
				.containsEntry("annotation2", "\"value2,a,b,c,d\"");
		assertThat(PropertyParserUtils.getStringPairsToMap("annotation1:\"value1,a,b,c,d\",annotation2:\"value2,a,b,c,d\""))
				.isNotEmpty()
				.hasSize(2)
				.containsEntry("annotation1", "\"value1,a,b,c,d\"")
				.containsEntry("annotation2", "\"value2,a,b,c,d\"");
// Test even number of quotes not to be used as token for ignoring commas boundary.
		assertThat(PropertyParserUtils.getStringPairsToMap("annotation1:\"value1,a,b,\"\"c,d\",annotation2:\"value2,a,b,c,d\""))
				.isNotEmpty()
				.hasSize(2)
				.containsEntry("annotation1", "\"value1,a,b,\"\"c,d\"")
				.containsEntry("annotation2", "\"value2,a,b,c,d\"");
	}

	@Test
	public void testAnnotationWithQuotes() {
		Map<String, String> annotations = PropertyParserUtils.getStringPairsToMap("annotation1:\"value1\",annotation2:value2");
		assertThat(annotations.isEmpty()).isFalse();
		assertThat(annotations.size() == 2).isTrue();
		assertThat(annotations.containsKey("annotation1")).isTrue();
		assertThat("\"value1\"".equals(annotations.get("annotation1"))).isTrue();
		assertThat(annotations.containsKey("annotation2")).isTrue();
		assertThat("value2".equals(annotations.get("annotation2"))).isTrue();
	}

	@Test
	public void testAnnotationMultipleColon() {
		String annotation = "iam.amazonaws.com/role:arn:aws:iam::12345678:role/role-name,key1:val1:val2:val3," +
				"key2:val4::val5:val6::val7:val8";
		Map<String, String> annotations = PropertyParserUtils.getStringPairsToMap(annotation);
		assertThat(annotations.isEmpty()).isFalse();
		assertThat(annotations.size() == 3).isTrue();
		assertThat(annotations.containsKey("iam.amazonaws.com/role")).isTrue();
		assertThat("arn:aws:iam::12345678:role/role-name".equals(annotations.get("iam.amazonaws.com/role"))).isTrue();
		assertThat(annotations.containsKey("key1")).isTrue();
		assertThat("val1:val2:val3".equals(annotations.get("key1"))).isTrue();
		assertThat(annotations.containsKey("key2")).isTrue();
		assertThat("val4::val5:val6::val7:val8".equals(annotations.get("key2"))).isTrue();
	}

	@Test
	public void testAnnotationParseInvalidValue() {
		assertThatThrownBy(() -> {
			PropertyParserUtils.getStringPairsToMap("annotation1:value1,annotation2,annotation3:value3");
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testDeploymentPropertyParsing() {
		Map<String, String> deploymentProps = new HashMap<>();
		deploymentProps.put("SPRING_CLOUD_DEPLOYER_KUBERNETES_IMAGEPULLPOLICY", "Never");
		deploymentProps.put("spring.cloud.deployer.kubernetes.pod-annotations", "key1:value1,key2:value2");
		deploymentProps.put("spring.cloud.deployer.kubernetes.serviceAnnotations", "key3:value3,key4:value4");
		deploymentProps.put("spring.cloud.deployer.kubernetes.init-container.image-name", "springcloud/openjdk");
		deploymentProps.put("spring.cloud.deployer.kubernetes.initContainer.containerName", "test");
		deploymentProps.put("spring.cloud.deployer.kubernetes.shareProcessNamespace", "true");
		deploymentProps.put("spring.cloud.deployer.kubernetes.priority-class-name", "high-priority");
		deploymentProps.put("spring.cloud.deployer.kubernetes.init-container.commands", "['sh','echo hello']");
		assertThat("key1:value1,key2:value2".equals(PropertyParserUtils.getDeploymentPropertyValue(deploymentProps, "spring.cloud.deployer.kubernetes.podAnnotations"))).isTrue();
		assertThat("key3:value3,key4:value4".equals(PropertyParserUtils.getDeploymentPropertyValue(deploymentProps, "spring.cloud.deployer.kubernetes.serviceAnnotations"))).isTrue();
		assertThat("springcloud/openjdk".equals(PropertyParserUtils.getDeploymentPropertyValue(deploymentProps, "spring.cloud.deployer.kubernetes.initContainer.imageName"))).isTrue();
		assertThat("springcloud/openjdk".equals(PropertyParserUtils.getDeploymentPropertyValue(deploymentProps, "spring.cloud.deployer.kubernetes.initContainer.imageName"))).isTrue();
		assertThat("Never".equals(PropertyParserUtils.getDeploymentPropertyValue(deploymentProps, "spring.cloud.deployer.kubernetes.imagePullPolicy"))).isTrue();
		assertThat("high-priority".equals(PropertyParserUtils.getDeploymentPropertyValue(deploymentProps, "spring.cloud.deployer.kubernetes.priority-class-name"))).isTrue();
		assertThat("true".equals(PropertyParserUtils.getDeploymentPropertyValue(deploymentProps, "spring.cloud.deployer.kubernetes.shareProcessNamespace"))).isTrue();
	}
}
