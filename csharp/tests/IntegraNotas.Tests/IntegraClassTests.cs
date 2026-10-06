using System.Net;
using IntegraNotas.Tests.Helpers;
using Xunit;

namespace IntegraNotas.Tests;

public class IntegraClassTests
{
    [Theory]
    [InlineData("")]
    [InlineData("   ")]
    [InlineData(null)]
    public void Construtor_SemToken_DeveLancarArgumentException(string? tokenInvalido)
    {
        var ex = Assert.Throws<ArgumentException>(() => new IntegraClass(token: tokenInvalido!));
        Assert.Contains("Token é obrigatório.", ex.Message);
    }

    [Fact]
    public async Task CriaAsync_DeveEnviarParaEndpointServicoComPost()
    {
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, "{\"id\":123}");
        using var httpClient = new HttpClient(handler);
        using var client = new IntegraClass(isProduction: false, token: "MEU_TOKEN", debug: false, httpClient: httpClient);

        var payload = new { emitente = "12345678000195" };
        var resposta = await client.CriaAsync(payload);

        Assert.Equal("{\"id\":123}", resposta);
        Assert.NotNull(handler.LastRequest);
        Assert.Equal(HttpMethod.Post, handler.LastRequest!.Method);
        Assert.EndsWith("/servico", handler.LastRequest.RequestUri?.AbsolutePath);
        Assert.True(handler.LastRequest.Headers.Contains("Authorization"));
        Assert.Equal("MEU_TOKEN", handler.LastRequest.Headers.GetValues("Authorization").First());
    }

    [Fact]
    public async Task ConsultaAsync_ComChaveValida_DeveConsultarEndpointGet()
    {
        string chave = "35230912345678000195550010000000011234567890";
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, "{\"status\":\"autorizada\"}");
        using var httpClient = new HttpClient(handler);
        using var client = new IntegraClass(isProduction: false, token: "MEU_TOKEN", debug: false, httpClient: httpClient);

        var resposta = await client.ConsultaAsync(chave);

        Assert.Equal("{\"status\":\"autorizada\"}", resposta);
        Assert.NotNull(handler.LastRequest);
        Assert.Equal(HttpMethod.Get, handler.LastRequest!.Method);
        Assert.EndsWith($"/servico/{chave}", handler.LastRequest.RequestUri?.AbsolutePath);
    }

    [Fact]
    public async Task ConsultaAsync_ComChaveInvalida_DeveLancarArgumentException()
    {
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, "{}");
        using var httpClient = new HttpClient(handler);
        using var client = new IntegraClass(isProduction: false, token: "MEU_TOKEN", debug: false, httpClient: httpClient);

        await Assert.ThrowsAsync<ArgumentException>(() => client.ConsultaAsync("12345invalida"));
    }

    [Fact]
    public async Task CancelaAsync_DeveEnviarParaEndpointCancelaComPost()
    {
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, "{\"cancelado\":true}");
        using var httpClient = new HttpClient(handler);
        using var client = new IntegraClass(isProduction: false, token: "MEU_TOKEN", debug: false, httpClient: httpClient);

        var payload = new { chave = "35230912345678000195550010000000011234567890", justificativa = "Cancelamento por desacordo" };
        var resposta = await client.CancelaAsync(payload);

        Assert.Equal("{\"cancelado\":true}", resposta);
        Assert.NotNull(handler.LastRequest);
        Assert.Equal(HttpMethod.Post, handler.LastRequest!.Method);
        Assert.EndsWith("/servico/cancela", handler.LastRequest.RequestUri?.AbsolutePath);
    }

    [Fact]
    public async Task PreviewAsync_DeveEnviarParaEndpointPreviewComPost()
    {
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, "{\"pdf_base64\":\"JVBER...\"}");
        using var httpClient = new HttpClient(handler);
        using var client = new IntegraClass(isProduction: false, token: "MEU_TOKEN", debug: false, httpClient: httpClient);

        var payload = new { emitente = "12345678000195" };
        var resposta = await client.PreviewAsync(payload);

        Assert.Equal("{\"pdf_base64\":\"JVBER...\"}", resposta);
        Assert.NotNull(handler.LastRequest);
        Assert.Equal(HttpMethod.Post, handler.LastRequest!.Method);
        Assert.EndsWith("/servico/preview", handler.LastRequest.RequestUri?.AbsolutePath);
    }
}
