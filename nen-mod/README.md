# Nen - mod de Hunter x Hunter (Fabric 1.21.1)

## Como compilar
1. Instale o JDK 21.
2. Nesta pasta rode `gradle wrapper` e depois `./gradlew build`.
3. O .jar sai em `build/libs/`. Coloque em `mods/` junto com o Fabric API.

## Como jogar
- **Copo de Divinação** (garrafa de vidro + folha de carvalho): clique direito para descobrir seu tipo de Nen.
- **Licença de Hunter** (ouro + papel + esmeralda):
  - Agachado + clique direito: alterna Nenhum -> Ten -> Ren -> Zetsu.
  - Clique direito: Hatsu do seu tipo (custa fome, 2s de recarga).
- Ten: resistência. Ren: força + velocidade (gasta fome). Zetsu: invisibilidade.

## Hatsu por tipo
- Intensificação: soco poderoso com repulsão (5 blocos)
- Emissão: raio de aura à distância (24 blocos) que queima
- Transformação: relâmpago no alvo
- Materialização: correntes que prendem e destacam inimigos próximos
- Manipulação: enfraquece e faz mobs largarem o alvo
- Especialização: cura, regeneração e absorção

Para refazer o sorteio: `/tag @s remove nen_type_enhancer` (ou o tipo que saiu).
