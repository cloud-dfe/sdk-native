using IntegraNotas.Examples;

string comando = args.Length > 0 ? args[0].ToLowerInvariant() : "ajuda";

Console.WriteLine("==================================================");
Console.WriteLine(" IntegraNotas - Exemplos Nativos em C# (.NET 8)");
Console.WriteLine("==================================================");

switch (comando)
{
    case "cria":
        Console.WriteLine("\n[Executando Exemplo Cria...]");
        await ExemploCria.ExecutarAsync();
        break;

    case "consulta":
        Console.WriteLine("\n[Executando Exemplo Consulta...]");
        await ExemploConsulta.ExecutarAsync();
        break;

    case "cancela":
        Console.WriteLine("\n[Executando Exemplo Cancela...]");
        await ExemploCancela.ExecutarAsync();
        break;

    case "preview":
        Console.WriteLine("\n[Executando Exemplo Preview...]");
        await ExemploPreview.ExecutarAsync();
        break;

    default:
        Console.WriteLine("\nUso:");
        Console.WriteLine("  dotnet run -- cria      - Executa o exemplo de emissão/criação");
        Console.WriteLine("  dotnet run -- consulta  - Executa o exemplo de consulta por chave");
        Console.WriteLine("  dotnet run -- cancela   - Executa o exemplo de cancelamento");
        Console.WriteLine("  dotnet run -- preview   - Executa o exemplo de pré-visualização");
        Console.WriteLine("\nExecutando demonstração do validador e inicialização:");
        await ExemploCria.ExecutarAsync();
        break;
}
