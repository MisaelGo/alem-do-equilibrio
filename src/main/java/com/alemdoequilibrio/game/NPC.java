package com.alemdoequilibrio.game;

import java.util.Arrays;
import java.util.List;

public class NPC extends GameCharacter {

    private String name;
    private List<String> dialogues;
    private int currentDialogueIndex = 0;
    private double x;
    private double y;
    private double width;
    private double height;

    public NPC(String name, double x, double y, double width, double height, String... dialogues) {
        super();
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dialogues = Arrays.asList(dialogues);
    }

    public String interact() {
        if (dialogues == null || dialogues.isEmpty()) {
            return "...";
        }
        
        String text = dialogues.get(currentDialogueIndex);
        currentDialogueIndex = (currentDialogueIndex + 1) % dialogues.size();
        return text;
    }

    public void resetDialogue() {
        this.currentDialogueIndex = 0;
    }

    @Override
    public void updateState() {
        // Lógica de atualização de estado do NPC caso precise
    }

    // Getters
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}