/**
 *
 * Luna Video Mapping |  Daniel Corbani - https://luna.art.br/
 *
 * https://github.com/danielcorbani/LunaMapping
 *
 * Uma biblioteca Processing/Java para Video Mapping.
 * Licença GPLv3: https://www.gnu.org/licenses/gpl-3.0.html
 *
 */

/**
 *
 * Exemplo que permite o mapeamento de cenas com multiplos arquivos
 *
 * --- controles ---
 * 'c'   ... [calibrate] calibração on/off
 * mouse ... movimentação dos pontos de controle do grid
 * 'b'   ... [background] mostrar/esconder cenário de fundo
 * 'i'   ... [input/outut] mostrar calibração de entrada/saída
 * 's'   ... [save] salvar ajustes
 * 'l'   ... [load] carregar ajustes
 * 'r'   ... [reset] voltar imagem para forma original
 * 'p'   ... [play/pause] roda/para os videos
 * 'm'   ... [switch media] troca o video sendo calibrado
 * 'n'   ... [next] próxima cena
 */
// ajustar uma vez no início do programa
// true: manda imagem para o projetor, se conectado;
// false: abre uma janela pequena para ajustes
boolean projetar = false;

//apresenta cenário ao fundo para ajustes
//pode ser ligado e desligado pela tecla 'b'
boolean mostrarCenario = true;

//Coloque o nome do arquivo entre aspas -> "nome.jpg"
String cenario = "cenario.jpg";

//Altere para 'true' após ter feito todos os ajustes.
//Isso vai fazer os ajustes serem carregados no início do programa
boolean carregarAjustes = true;
boolean loadAll = carregarAjustes;

import paletai.mapping.*;
import processing.video.*;
PImage background;

ArrayList<Scene> cenas;
int activeSceneIndex = 0;
float transitionAlpha = 0;
boolean transitioning = false;
int numMedia = 0, selectMedia = 0;

void settings() {
  if (projetar) {
    fullScreen(P2D, 2);
  } else {
    size(960, 540, P2D);
  }
}

void setup() {
  background = loadImage(cenario);
  if (float(width)/float(background.width) > float(height)/float(background.height)) background.resize(width, 0);
  cenas = new ArrayList<Scene>();

  Scene cena1 = new Scene(this, 1);
  cenas.add(cena1);

  Scene cena2 = new Scene(this, 2);
  cena2.addMediaItem(new MediaItem(this, dataPath("sunset.mp4"), 2));
  cena2.addMediaItem(new MediaItem(this, dataPath("bailarina.mp4"), 2));
  cenas.add(cena2);

  Scene cena3 = new Scene(this, 3);
  cena3.addMediaItem(new MediaItem(this, dataPath("fumaca.mp4"), 3));
  cena3.addMediaItem(new MediaItem(this, dataPath("silhueta.mp4"), 3));
  cenas.add(cena3);

  Scene cena4 = new Scene(this, 4);
  cena4.addMediaItem(new MediaItem(this, dataPath("tchau.mp4"), 4));
  cenas.add(cena4);

  // Start with the first scene active
  cenas.get(activeSceneIndex).setActive(true);
  cenas.get(activeSceneIndex).loadAll();
  numMedia = cenas.get(activeSceneIndex).getMediaItems().size();
}


void draw() {
  background(0);
  if (mostrarCenario) image(background, 0, 0);

  cenas.get(activeSceneIndex).render(mouseX, mouseY);

  //if (loadAll) {
  //  cenas.get(activeSceneIndex).loadAll();
  //  loadAll = false;
  //}
  if (transitioning) {
    transitionEffect();
  }
}

void transitionEffect() {
  transitionAlpha += 5;
  int nextSceneIndex = (activeSceneIndex + 1) % cenas.size();

  // Activate next scene while still transitioning
  cenas.get(nextSceneIndex).setActive(true);
  if (loadAll) cenas.get(nextSceneIndex).loadAll();
  pushStyle();
  tint(255, transitionAlpha);
  cenas.get(nextSceneIndex).render(mouseX, mouseY);
  popStyle();

  if (transitionAlpha >= 255) {
    cenas.get(activeSceneIndex).setActive(false); // Deactivate previous scene
    activeSceneIndex = nextSceneIndex;
    println("activeSceneIndex = " +activeSceneIndex);
    transitioning = false;
    transitionAlpha = 0;
    numMedia = cenas.get(activeSceneIndex).getMediaItems().size();
    loadAll = carregarAjustes;
  }
}


void keyPressed() {
  if (key == 'n' && !transitioning) {
    transitioning = true;
    transitionAlpha = 0;
  } else if (key == 'c') {
    cenas.get(activeSceneIndex).toggleCalibration();
  } else if (key == 'm') {
    if (!transitioning) {
      cenas.get(activeSceneIndex).offCalibration();
      selectMedia = (selectMedia +1)%numMedia;
      cenas.get(activeSceneIndex).switchActiveMedia(selectMedia);
      cenas.get(activeSceneIndex).onCalibration();
    }
  } else if (key == 's') {
    for (Scene c : cenas) {
      c.saveAll();
    }
  } else if (key == 'l') {
    for (Scene c : cenas) {
      c.loadAll();
    }
  } else if (key == 'u') {
    for (Scene c : cenas) {
      c.toggleLoop(); // Toggle video playback
    }
  } else if (key == 'b') {
    mostrarCenario = !mostrarCenario;
  } else if (key == 'i') {
    cenas.get(activeSceneIndex).toggleInput();
  }
}

void mouseDragged() {
  cenas.get(activeSceneIndex).moveHoverPoint(mouseX, mouseY);
}
