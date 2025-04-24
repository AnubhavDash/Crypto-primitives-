/*
 * Copyright 2025 Swiss Post Ltd
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
package ch.post.it.evoting.cryptoprimitives.internal.mixnet;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupMatrix;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationStatement;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationWitness;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;

public class TestMultiExponentiationStatementWitnessPairGenerator {

	private final GqGroup gqGroup;
	private final ZqGroup zqGroup;
	private final ElGamalGenerator elGamalGenerator;
	private final ZqGroupGenerator zqGroupGenerator;
	private final MultiExponentiationArgumentService argumentService;
	private final CommitmentKey commitmentKey;

	TestMultiExponentiationStatementWitnessPairGenerator(final GqGroup group, final MultiExponentiationArgumentService argumentService,
			final CommitmentKey commitmentKey) {
		this.gqGroup = group;
		this.zqGroup = ZqGroup.sameOrderAs(gqGroup);
		this.zqGroupGenerator = new ZqGroupGenerator(zqGroup);
		this.elGamalGenerator = new ElGamalGenerator(gqGroup);
		this.argumentService = argumentService;
		this.commitmentKey = commitmentKey;
	}

	record StatementWitnessPair(MultiExponentiationStatement statement, MultiExponentiationWitness witness) {
	}

	StatementWitnessPair genPair(final int n, final int m, final int l) {
		final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> CMatrix = this.elGamalGenerator.genRandomCiphertextMatrix(m, n, l);
		final GroupMatrix<ZqElement, ZqGroup> AMatrix = zqGroupGenerator.genRandomZqElementMatrix(n, m);
		final GroupVector<ZqElement, ZqGroup> rExponents = zqGroupGenerator.genRandomZqElementVector(m);
		final ZqElement rhoExponents = zqGroupGenerator.genRandomZqElementMember();

		final ElGamalMultiRecipientCiphertext computedC = argumentService.multiExponentiation(CMatrix, AMatrix, rhoExponents, m, l);
		final GroupVector<GqElement, GqGroup> commitmentToA = CommitmentService.getCommitmentMatrix(
				AMatrix, rExponents, commitmentKey);
		final MultiExponentiationStatement statement = new MultiExponentiationStatement(CMatrix, computedC, commitmentToA);
		final MultiExponentiationWitness witness = new MultiExponentiationWitness(AMatrix, rExponents, rhoExponents);
		return new StatementWitnessPair(statement, witness);
	}
}
