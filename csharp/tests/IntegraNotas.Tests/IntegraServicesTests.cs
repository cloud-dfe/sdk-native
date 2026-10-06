using System.Net;
using IntegraNotas.Tests.Helpers;
using Xunit;

namespace IntegraNotas.Tests;

public class IntegraServicesTests
{
    [Fact]
    public void BaseUri_QuandoProducao_DeveUtilizarUriProducao()
    {
        using var services = new IntegraServices(isProduction: true, version: "1");
        Assert.Equal(IntegraServices.UriProduction, services.BaseUri);
        Assert.True(services.IsProduction);
    }

    [Fact]
    public void BaseUri_QuandoHomologacao_DeveUtilizarUriTestes()
    {
        using var services = new IntegraServices(isProduction: false, version: "1");
        Assert.Equal(IntegraServices.UriTests, services.BaseUri);
        Assert.False(services.IsProduction);
    }

    [Fact]
    public async Task RequestAsync_ComRespostaValida_DeveRetornarJson()
    {
        string jsonEsperado = "{\"status\":\"sucesso\",\"codigo\":200}";
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, jsonEsperado);
        using var httpClient = new HttpClient(handler);
        using var services = new IntegraServices(isProduction: false, version: "1", debug: false, httpClient: httpClient);

        string resultado = await services.RequestAsync("POST", "/servico", new { chave = "123" });

        Assert.Equal(jsonEsperado, resultado);
        Assert.NotNull(handler.LastRequest);
        Assert.Equal(HttpMethod.Post, handler.LastRequest!.Method);
        Assert.Equal("https://hom-api.integranotas.com.br/v1/servico", handler.LastRequest.RequestUri?.ToString());
    }

    [Fact]
    public async Task RequestAsync_ComRespostaErroHttp_DeveLancarIntegraExceptionComStatus()
    {
        string jsonErro = "{\"error\":\"Documento não encontrado\"}";
        var handler = new MockHttpMessageHandler(HttpStatusCode.NotFound, jsonErro);
        using var httpClient = new HttpClient(handler);
        using var services = new IntegraServices(isProduction: false, version: "1", debug: false, httpClient: httpClient);

        var ex = await Assert.ThrowsAsync<IntegraException>(() =>
            services.RequestAsync("GET", "/servico/35230912345678000195550010000000011234567890"));

        Assert.Equal(404, ex.HttpCode);
        Assert.Equal(jsonErro, ex.ResponseBody);
    }

    [Fact]
    public async Task RequestAsync_ComRespostaVazia_DeveLancarIntegraException()
    {
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, "   ");
        using var httpClient = new HttpClient(handler);
        using var services = new IntegraServices(isProduction: false, version: "1", debug: false, httpClient: httpClient);

        var ex = await Assert.ThrowsAsync<IntegraException>(() =>
            services.RequestAsync("POST", "/servico", new { }));

        Assert.Contains("Resposta Vazia", ex.Message);
    }

    [Fact]
    public async Task RequestAsync_ComJsonInvalido_DeveLancarIntegraException()
    {
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, "<html>Erro no servidor</html>");
        using var httpClient = new HttpClient(handler);
        using var services = new IntegraServices(isProduction: false, version: "1", debug: false, httpClient: httpClient);

        var ex = await Assert.ThrowsAsync<IntegraException>(() =>
            services.RequestAsync("POST", "/servico", new { }));

        Assert.Contains("Resposta JSON Inválida", ex.Message);
    }

    [Fact]
    public async Task RequestAsync_ComModoDebugAtivo_DeveIncluirDadosDeDebugNaResposta()
    {
        string jsonEsperado = "{\"sucesso\":true}";
        var handler = new MockHttpMessageHandler(HttpStatusCode.OK, jsonEsperado);
        using var httpClient = new HttpClient(handler);
        using var services = new IntegraServices(isProduction: false, version: "1", debug: true, httpClient: httpClient);

        string resultado = await services.RequestAsync("POST", "/servico", new { teste = 1 });

        Assert.Contains("\"debug\"", resultado);
        Assert.Contains("\"method\": \"POST\"", resultado);
        Assert.Contains("\"data\"", resultado);
    }
}
