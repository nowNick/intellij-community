/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public interface DebuggableRunConfiguration extends RunConfiguration {
  @NotNull
  InetSocketAddress computeDebugAddress() throws ExecutionException;

  @NotNull
  XDebugProcess createDebugProcess(@NotNull InetSocketAddress socketAddress,
                                   @NotNull XDebugSession session,
                                   @Nullable ExecutionResult executionResult,
                                   @NotNull ExecutionEnvironment environment) throws ExecutionException;

  interface RunConfigurationWithConditionalRun {
    boolean canRun(@NotNull String executorId, @NotNull RunProfile profile);
  }
}