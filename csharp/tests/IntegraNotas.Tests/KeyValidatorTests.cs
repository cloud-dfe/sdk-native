using IntegraNotas.Validation;
using Xunit;

namespace IntegraNotas.Tests;

public class KeyValidatorTests
{
    [Fact]
    public void CheckKey_ComChave44Digitos_DeveRetornarChaveLimpa()
    {
        // 44 dígitos válidos para DFe/NFe
        string chaveOriginal = "35230912345678000195550010000000011234567890";
        string resultado = KeyValidator.CheckKey(chaveOriginal);

        Assert.Equal(chaveOriginal, resultado);
    }

    [Fact]
    public void CheckKey_ComChave50Digitos_DeveRetornarChaveLimpa()
    {
        // 50 dígitos válidos para NFSe
        string chaveOriginal = "35230912345678000195550010000000011234567890123456";
        string resultado = KeyValidator.CheckKey(chaveOriginal);

        Assert.Equal(chaveOriginal, resultado);
    }

    [Fact]
    public void CheckKey_ComPontuacoesEEspacos_DeveRemoverNaoNumericosERetornar()
    {
        string chaveFormatada = "3523.0912.3456.7800.0195-5500.1000.0000.0112-3456.7890";
        string resultado = KeyValidator.CheckKey(chaveFormatada);

        Assert.Equal(44, resultado.Length);
        Assert.DoesNotContain(".", resultado);
        Assert.DoesNotContain("-", resultado);
    }

    [Theory]
    [InlineData("")]
    [InlineData(null)]
    [InlineData("12345")]
    [InlineData("3523091234567800019555001000000001123456789")] // 43 dígitos
    [InlineData("352309123456780001955500100000000112345678901")] // 45 dígitos
    [InlineData("352309123456780001955500100000000112345678901234567")] // 51 dígitos
    public void CheckKey_ComTamanhoInvalido_DeveLancarArgumentException(string? chaveInvalida)
    {
        var ex = Assert.Throws<ArgumentException>(() => KeyValidator.CheckKey(chaveInvalida));
        Assert.Contains("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.", ex.Message);
    }
}
