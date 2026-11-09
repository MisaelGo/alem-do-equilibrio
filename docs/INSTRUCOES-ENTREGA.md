# Entrega do projeto de POO

1. Antes de gerar o ZIP, abra o projeto no **NetBeans com JDK 26** e execute `mvn verify`, depois `mvn javafx:run`.
2. Verifique a [lista dos 22 itens](indice-22-itens-poo.md), especialmente o nome, a justificativa e a localização dos exemplos nos comentários do código.
3. O diagrama **oficial escolhido pela equipe** é [diagrama-classes.puml](diagrama-classes.puml). Abra no PlantUML do NetBeans e exporte em SVG ou PDF, conferindo a legibilidade. O antigo [diagrama Graphviz](diagrama-classes.dot) é uma alternativa, não o modelo de apresentação.
4. Instale [Graphviz](https://graphviz.org/download/) se necessário e confira no terminal: `dot -V`.
5. Na raiz do repositório, execute `python scripts/gerar-entrega.py Nome1_Nome2_Nome3_Nome4`, substituindo pelos primeiros nomes **reais** da equipe, separados por `_`.
6. Confira o arquivo `dist/Nome1_Nome2_Nome3_Nome4.zip`, que inclui o projeto e o índice. O gerador antigo produz um PDF/SVG pelo Graphviz; **substitua esses PDFs/SVGs pelo resultado exportado do `diagrama-classes.puml` no NetBeans** no ZIP final, para que o desenho entregue seja exatamente o escolhido pela equipe.
7. Entregue o ZIP **até o dia anterior à apresentação**. O professor exige a demonstração dos 22 itens, a participação de todos e que os itens apareçam no índice **e** no diagrama.

O script falha explicitamente se houver mais de um método `main`, se houver pacote adicional, se faltarem os 22 itens no índice ou no diagrama, se o Graphviz não gerar o PDF, ou se o ZIP estiver corrompido. **Não** envia o ZIP automaticamente ao professor.
