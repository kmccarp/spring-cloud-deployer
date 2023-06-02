/*
 * Copyright 2016-2019 the original author or authors.
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

package org.springframework.cloud.deployer.spi.task;

import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.spi.core.RuntimeEnvironmentInfo;

/**
 * SPI defining a runtime environment capable of launching and managing the
 * lifecycle of tasks. The term 'task' in the context of a {@code TaskLauncher}
 * is merely a runtime representation of a container or application wherein the
 * task should be executed.
 *
 * The SPI itself doesn't expect the launcher to keep state of launched tasks,
 * meaning it doesn't need to reconstruct all existing {@link TaskStatus}es or
 * the IDs needed to resolve a {@link TaskStatus}. The responsibility for
 * keeping track of the state of all existing tasks lies to whomever is using
 * this SPI. It is unrealistic to expect the launcher to be able to store enough
 * information using the underlying infrastructure to reconstruct the full
 * history of task executions.
 *
 * @author Mark Fisher
 * @author Janne Valkealahti
 * @author David Turanski
 * @author Ilayaperumal Gopinathan
 */
public interface TaskLauncher {

	/**
	 * Launch a task for the provided {@link AppDeploymentRequest}. The returned
	 * id may later be used with {@link #cancel(String)} or
	 * {@link #status(String)} to cancel a task or get its status,
	 * respectively.
	 *
	 * Implementations may perform this operation asynchronously; therefore a
	 * successful launch may not be assumed upon return. To determine the status
	 * of a launch, invoke {@link #status(String)}.
	 *
	 * @param request the task launch request
	 * @return the id for the launched task
	 */
	String launch(AppDeploymentRequest request);

	/**
	 * Cancel the task corresponding to the provided id.
	 *
	 * Implementations may perform this operation asynchronously; therefore a
	 * successful cancellation may not be assumed upon return. To determine the
	 * status of a cancellation, invoke {@link #status(String)}.
	 *
	 * @param id the task id, as returned by {@link #launch(AppDeploymentRequest)}
	 */
	void cancel(String id);

	/**
	 * Returns the {@link TaskStatus} for a task represented by the provided id.
	 *
	 * @param id the task id, as returned by {@link #launch(AppDeploymentRequest)}
	 * @return the task status
	 */
	TaskStatus status(String id);

	/**
	 * Attempt to clean up any app execution resources that are associated with a task launch represented by the
	 * provided task execution id.
	 *
	 * Implementations can choose to ignore this request if the underlying platform does not have any resources
	 * associated with the task launch. Implementations may perform this operation asynchronously; therefore a
	 * successful clean up may not be assumed upon return.
	 *
	 * @param id the task id, as returned by {@link #launch(AppDeploymentRequest)}
	 */
	void cleanup(String id);

	/**
	 * Attempt to clean up any app resources that are associated with a task app represented by the provided
	 * appName. Any app execution resources from all task launches for this app should be cleaned up as well.
	 *
	 * Implementations can choose to ignore this request if the underlying platform does not have any resources
	 * associated with the task app and if there are no task launch resources created for this app. Implementations
	 * may perform this operation asynchronously; therefore a successful clean up may not be assumed upon return.
	 *
	 * @param appName the app name as specified in {@link org.springframework.cloud.deployer.spi.core.AppDefinition#name}
	 *                from the {@link #launch(AppDeploymentRequest)}
	 */
	void destroy(String appName);

	/**
	 * Return the environment info for this launcher/deployer.
	 *
	 * @return the deployer environment info
	 */
	RuntimeEnvironmentInfo environmentInfo();

	/**
	 * Implementations may limit the number of concurrent task executions.
	 *
	 * @return the maximum concurrent task executions allowed.
	 */
	default int getMaximumConcurrentTasks() {
		throw new UnsupportedOperationException("'getMaximumConcurrentTasks' is not implemented.");
	}

	/**
	 *
	 * @return the count of currently running task executions if known.
	 */
	default int getRunningTaskExecutionCount() {
		throw new UnsupportedOperationException("'getRunningTaskExecutionCount' is not implemented.");
	}

	/**
	 * Return the log of the application identified by the task ID.
	 * The ID can be specific to the platform where the task is launched.
	 *
	 * @return the task application log
	 */
	default String getLog(String id) {
		throw new UnsupportedOperationException("'getLog' is not implemented.");
	}
}
