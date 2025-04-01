/*
 * 
 * Luna Video Mapping |  Daniel Corbani - https://luna.art.br/
 * 
 * https://github.com/danielcorbani/LunaMapping
 * 
 * Uma biblioteca Processing/Java para Video Mapping.
 * Licença GPLv3: https://www.gnu.org/licenses/gpl-3.0.html
 * 
 */
  
/*
 * 
 * Exemplo inicial que permite o mapeamento de um único arquivo
 * 
 * --- controles ---
 * 'c'   ... [calibrate] calibração on/off
 * mouse ... movimentação dos pontos de controle do grid
 * 'b'   ... [background] mostrar/esconder cenário de fundo
 * 'i'   ... [input/outut] mostrar calibração de entrada/saída
 * 's'   ... [save] salvar ajustes
 * 'l'   ... [load] carregar ajustes
 * 'r'   ... [reset] voltar imagem para forma original
 */

// ajustar uma vez no início do programa
// true: manda imagem para o projetor, se conectado;
// false: abre uma janela pequena para ajustes
boolean projetar = false;

//apresenta cenário ao fundo para ajustes
//pode ser ligado e desligado pela tecla 'b'
boolean mostrarCenario = false;

//Coloque o nome do arquivo entre aspas -> "nome.jpg"
String cenario = "auroraEnsaio.jpg";

//Altere para 'true' após ter feito todos os ajustes.
//Isso vai fazer os ajustes serem carregados no início do programa
boolean carregarAjustes = false;

import paletai.mapping.*;
import processing.video.*;
PImage background;

String imagem = "quadro.jpg";
MediaItem media;

void settings() {
  if (projetar) {
    fullScreen(P2D, 2);
  } else {
    size(720, 540, P2D);
  }
}

void setup() {
  background = loadImage(cenario);
  if (float(width)/float(background.width) < float(height)/float(background.height)) background.resize(width, 0);
  else background.resize(0, height);
  media = new MediaItem(this, imagem, 1);
  if (carregarAjustes) media.loadHomography();
}


void draw() {
  background(0);
  if (mostrarCenario) image(background, 0, 0);

  media.checkHover(mouseX, mouseY);
  media.render();
}

void mouseDragged() {
  media.moveHoverPoint(mouseX, mouseY);  // Move hovered point while dragging
}

void keyPressed() {
  if (key == 'c') {
    media.toggleCalibration();
  } else if (key == 's') {
    media.saveHomography();
  } else if (key == 'r') {
    media.resetHomography();
  } else if (key == 'l') {
    media.loadHomography();
  } else if (key == 'i') {
    media.toggleInput();
  } else if (key == 'b') {
    mostrarCenario = !mostrarCenario;
  }
}
