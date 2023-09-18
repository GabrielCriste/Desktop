/*
 * Copyright 2022 storch.dev
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

package torch
package nn
package modules
package activation

class ActivationSuite extends munit.FunSuite {
  test("LogSoftmax") {
    torch.manualSeed(0)
    val m = nn.LogSoftmax(dim = 1)
    val input = torch.randn(Seq(2, 3))
    val output = m(input)
    assertEquals(output.shape, input.shape)
    val expectedOutput = Tensor(
      Seq(
        Seq(-0.1689f, -2.0033f, -3.8886f),
        Seq(-0.2862f, -1.9392f, -2.2532f)
      )
    )
    assert(torch.allclose(output, expectedOutput, atol = 1e-4))
  }

  test("Tanh") {
    torch.manualSeed(0)
    val m = nn.Tanh()
    val input = torch.randn(Seq(2))
    val output = m(input)
    assertEquals(output.shape, input.shape)
    val expectedOutput = Tensor(Seq(0.9123f, -0.2853f))
    assert(torch.allclose(output, expectedOutput, atol = 1e-4))
  }

  test("BatchNorm1d") {
    torch.manualSeed(0)
    val m = nn.BatchNorm1d(numFeatures = 3)
    val input = torch.randn(Seq(3, 3))
    val output = m(input)
    assertEquals(output.shape, input.shape)
    val expectedOutput = Tensor(
      Seq(
        Seq(1.4014f, -0.1438f, -1.2519f),
        Seq(-0.5362f, -1.1465f, 0.0564f),
        Seq(-0.8651f, 1.2903f, 1.1956f)
      )
    )
    assert(torch.allclose(output, expectedOutput, atol = 1e-4))
  }

  test("BatchNorm2d") {
    torch.manualSeed(0)
    val m = nn.BatchNorm2d(numFeatures = 3)
    val input = torch.randn(Seq(3, 3, 1, 1))
    val output = m(input)
    assertEquals(output.shape, input.shape)
    val expectedOutput = Tensor(
      Seq(
        Seq(1.4014f, -0.1438f, -1.2519f),
        Seq(-0.5362f, -1.1465f, 0.0564f),
        Seq(-0.8651f, 1.2903f, 1.1956f)
      )
    )
    assert(torch.allclose(output.squeeze, expectedOutput, atol = 1e-4))
  }

  test("LayerNorm") {
    {
      torch.manualSeed(0)
      val (batch, sentenceLength, embeddingDim) = (2, 2, 3)
      val embedding = torch.randn(Seq(batch, sentenceLength, embeddingDim))
      val layerNorm = nn.LayerNorm(embeddingDim)
      val output = layerNorm(embedding)
      assertEquals(output.shape, embedding.shape)
      val expectedOutput = Tensor(
        Seq(
          Seq(
            Seq(1.2191f, 0.0112f, -1.2303f),
            Seq(1.3985f, -0.5172f, -0.8813f)
          ),
          Seq(
            Seq(0.3495f, 1.0120f, -1.3615f),
            Seq(-0.3948f, -0.9786f, 1.3734f)
          )
        )
      )
      assert(torch.allclose(output, expectedOutput, atol = 1e-4))
    }
    {
      torch.manualSeed(0)
      val (n, c, h, w) = (1, 2, 2, 2)
      val input = torch.randn(Seq(n, c, h, w))
      // Normalize over the last three dimensions (i.e. the channel and spatial dimensions)
      val layerNorm = nn.LayerNorm(Seq(c, h, w))
      val output = layerNorm(input)
      assertEquals(output.shape, (Seq(n, c, h, w)))
      val expectedOutput = Tensor(
        Seq(
          Seq(
            Seq(1.4715f, -0.0785f),
            Seq(-1.6714f, 0.6497f)
          ),
          Seq(
            Seq(-0.7469f, -1.0122f),
            Seq(0.5103f, 0.8775f)
          )
        )
      ).unsqueeze(0)
      assert(torch.allclose(output, expectedOutput, atol = 1e-4))
    }
  }

  test("Embedding") {
    {
      torch.manualSeed(0)
      val embedding = nn.Embedding(10, 3)
      // a batch of 2 samples of 4 indices each
      val input = torch.Tensor(Seq(Seq(1L, 2, 4, 5), Seq(4L, 3, 2, 9)))
      val output = embedding(input)
      val expectedOutput = Tensor(
        Seq(
          Seq(
            Seq(-0.4339f, 0.8487f, 0.6920f),
            Seq(-0.3160f, -2.1152f, 0.3223f),
            Seq(0.1198f, 1.2377f, -0.1435f),
            Seq(-0.1116f, -0.6136f, 0.0316f)
          ),
          Seq(
            Seq(0.1198f, 1.2377f, -0.1435f),
            Seq(-1.2633f, 0.3500f, 0.3081f),
            Seq(-0.3160f, -2.1152f, 0.3223f),
            Seq(0.0525f, 0.5229f, 2.3022f)
          )
        )
      )
      assert(torch.allclose(output, expectedOutput, atol = 1e-4))
    }
    {
      torch.manualSeed(0)
      // example with padding_idx
      val embedding = nn.Embedding(5, 3, paddingIdx = Some(0))
      embedding.weight = Tensor[Float](
        Seq(
          Seq(0f, 0f, 0f),
          Seq(0.5684f, -1.0845f, -1.3986f),
          Seq(0.4033f, 0.8380f, -0.7193f),
          Seq(0.4033f, 0.8380f, -0.7193f),
          Seq(-0.8567f, 1.1006f, -1.0712f)
        )
      )
      val input = torch.Tensor(Seq(Seq(0L, 2, 0, 4)))
      val output = embedding(input)

      val expectedOutput = Tensor(
        Seq(
          Seq(0f, 0f, 0f),
          Seq(0.4033f, 0.8380f, -0.7193f),
          Seq(0f, 0f, 0f),
          Seq(-0.8567f, 1.1006f, -1.0712f)
        )
      ).unsqueeze(0)
      assert(torch.allclose(output, expectedOutput, atol = 1e-4))
    }
    // {
    //   torch.manualSeed(0)
    //   //  example of changing `pad` vector
    //   val paddingIdx = 0
    //   val embedding = nn.Embedding(3, 3, paddingIdx = Some(paddingIdx))
    //   noGrad {
    //     embedding.weight(Seq(paddingIdx)) = torch.ones(3)
    //   }
    //   val expectedOutput = Tensor(
    //     Seq(
    //       Seq(1f, 1f, 1f),
    //       Seq(0.5684f, -1.0845f, -1.3986f),
    //       Seq(0.4033f, 0.8380f, -0.7193f)
    //     )
    //   )
    //   assert(torch.allclose(embedding.weight, expectedOutput, atol = 1e-4))
    // }
  }
}
