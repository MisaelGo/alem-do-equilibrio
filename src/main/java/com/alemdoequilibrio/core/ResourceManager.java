package com.alemdoequilibrio.core;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;

/**
 * Centraliza o carregamento de recursos do jogo.
 *
 * O ResourceManager carrega Image, AudioClip e folhas de estilo CSS
 * a partir do classpath (pasta src/main/resources) e reutiliza cada
 * recurso já carregado, em vez de recriá-lo a cada chamada.
 *
 * Isso evita, por exemplo, que a mesma imagem de tile seja lida do
 * disco uma vez para cada inimigo em tela.
 */
/*
 * POO 1.1 - Encapsulamento:
 * os caches internos são privados; outras classes só têm acesso
 * aos recursos por meio dos métodos públicos desta classe.
 */
public class ResourceManager {

    /*
     * Cache de imagens já carregadas, indexadas pelo caminho
     * usado para carregá-las.
     */
    private final Map<String, Image> imageCache;

    /*
     * Cache de efeitos sonoros curtos já carregados.
     *
     * AudioClip é indicado para sons curtos (hit, quiz, etc.),
     * conforme definido no TDD. Trilhas longas devem usar
     * MediaPlayer, fora do escopo desta classe por enquanto.
     */
    private final Map<String, AudioClip> audioCache;

    /*
     * Cache das URLs externas resolvidas para folhas de estilo CSS.
     *
     * Scene/Parent do JavaFX esperam uma String de URL (não um
     * objeto próprio de CSS), então aqui só evitamos resolver
     * o mesmo caminho repetidamente.
     */
    private final Map<String, String> stylesheetCache;

    /**
     * Cria um ResourceManager com os caches vazios.
     *
     * Uma única instância deve ser criada e compartilhada entre
     * os sistemas que precisam carregar recursos (ver GameController),
     * para que o cache realmente sirva a todo o jogo.
     */
    public ResourceManager() {
        this.imageCache = new HashMap<>();
        this.audioCache = new HashMap<>();
        this.stylesheetCache = new HashMap<>();
    }

    /**
     * Retorna a imagem correspondente ao caminho informado.
     *
     * Se a imagem já tiver sido carregada anteriormente, a mesma
     * referência é devolvida. Caso contrário, ela é carregada uma
     * única vez e armazenada no cache antes de ser retornada.
     *
     * @param path caminho da imagem dentro de resources, começando
     *             com "/", por exemplo "/images/hero.png"
     * @return imagem carregada e cacheada
     * @throws IllegalArgumentException se o recurso não for encontrado
     */
    public Image getImage(String path) {

        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        Image image = new Image(resolve(path));

        imageCache.put(path, image);

        return image;
    }

    /**
     * Retorna o efeito sonoro correspondente ao caminho informado.
     *
     * Segue a mesma lógica de cache de {@link #getImage(String)}:
     * o arquivo é carregado apenas na primeira chamada.
     *
     * @param path caminho do áudio dentro de resources, por exemplo
     *             "/audio/sfx_quiz_correct.wav"
     * @return efeito sonoro carregado e cacheado
     * @throws IllegalArgumentException se o recurso não for encontrado
     */
    public AudioClip getAudioClip(String path) {

        if (audioCache.containsKey(path)) {
            return audioCache.get(path);
        }

        AudioClip clip = new AudioClip(resolveUrl(path).toExternalForm());

        audioCache.put(path, clip);

        return clip;
    }

    /**
     * Retorna a URL externa de uma folha de estilo CSS, pronta para
     * ser usada em {@code Scene.getStylesheets()} ou
     * {@code Parent.getStylesheets()}.
     *
     * @param path caminho do CSS dentro de resources, por exemplo
     *             "/css/hud.css"
     * @return URL externa da folha de estilo, já resolvida e cacheada
     * @throws IllegalArgumentException se o recurso não for encontrado
     */
    public String getStylesheet(String path) {

        if (stylesheetCache.containsKey(path)) {
            return stylesheetCache.get(path);
        }

        String url = resolveUrl(path).toExternalForm();

        stylesheetCache.put(path, url);

        return url;
    }

    /**
     * Informa quantas imagens distintas já foram carregadas.
     *
     * Método utilitário de apoio a testes/depuração, para confirmar
     * que um recurso pedido duas vezes não gera duas entradas no cache.
     *
     * @return quantidade de imagens atualmente em cache
     */
    public int getCachedImageCount() {
        return imageCache.size();
    }

    /*
     * Resolve o caminho de um recurso para a String de URL externa
     * usada pelo construtor de Image.
     *
     * Centralizar essa resolução aqui evita repetir a checagem de
     * "recurso não encontrado" em cada método público.
     */
    private String resolve(String path) {
        return resolveUrl(path).toExternalForm();
    }

    /*
     * Localiza o recurso no classpath (pasta resources) e valida
     * que ele realmente existe antes de tentar carregá-lo.
     */
    private java.net.URL resolveUrl(String path) {

        java.net.URL url = getClass().getResource(path);

        if (url == null) {
            throw new IllegalArgumentException(
                    "Recurso nao encontrado: " + path
                    + " (verifique se o arquivo esta em src/main/resources"
                    + " e se o caminho comeca com \"/\")"
            );
        }

        return url;
    }
}