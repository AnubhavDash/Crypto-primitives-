/*
 * Copyright 2024 Swiss Post Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.post.it.evoting.cryptoprimitives;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "ch.post.it.evoting.cryptoprimitives")
public class ArchitectureRulesTest {

	@ArchTest
	static final ArchRule NO_CLASSES_SHOULD_CALL_TO_LOWER_CASE_WITHOUT_LOCALE = noClasses().should().callMethod(String.class, "toLowerCase");

	@ArchTest
	static final ArchRule NO_CLASSES_SHOULD_CALL_TO_UPPER_CASE_WITHOUT_LOCALE = noClasses().should().callMethod(String.class, "toUpperCase");
}
