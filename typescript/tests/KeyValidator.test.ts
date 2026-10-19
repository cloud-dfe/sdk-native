import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { KeyValidator } from "../src/validation/KeyValidator.js";

describe("KeyValidator", () => {
  it("deve retornar chave limpa quando possuir 44 dígitos (DFe/NFe)", () => {
    const chaveOriginal = "35230912345678000195550010000000011234567890";
    const resultado = KeyValidator.checkKey(chaveOriginal);

    assert.equal(resultado, chaveOriginal);
    assert.equal(resultado.length, 44);
  });

  it("deve retornar chave limpa quando possuir 50 dígitos (NFSe)", () => {
    const chaveOriginal = "35230912345678000195550010000000011234567890123456";
    const resultado = KeyValidator.checkKey(chaveOriginal);

    assert.equal(resultado, chaveOriginal);
    assert.equal(resultado.length, 50);
  });

  it("deve remover caracteres não numéricos e validar tamanho correto", () => {
    const chaveFormatada = "3523.0912.3456.7800.0195-5500.1000.0000.0112-3456.7890";
    const resultado = KeyValidator.checkKey(chaveFormatada);

    assert.equal(resultado.length, 44);
    assert.doesNotMatch(resultado, /[.-]/);
  });

  const chavesInvalidas = [
    "",
    null,
    undefined,
    "12345",
    "3523091234567800019555001000000001123456789", // 43 dígitos
    "352309123456780001955500100000000112345678901", // 45 dígitos
    "352309123456780001955500100000000112345678901234567" // 51 dígitos
  ];

  for (const chave of chavesInvalidas) {
    it(`deve lançar erro para chave com formato/tamanho inválido: "${chave}"`, () => {
      assert.throws(
        () => KeyValidator.checkKey(chave as any),
        /A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe\./
      );
    });
  }
});
