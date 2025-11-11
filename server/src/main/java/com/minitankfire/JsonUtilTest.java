package com.minitankfire;

import java.util.Arrays;

/**
 * Simple test to verify JSON serialization works correctly
 */
public class JsonUtilTest {
    public static void main(String[] args) {
        System.out.println("Testing JSON Serialization...\n");

        // Test Player JSON
        Player testPlayer = new Player("test-id-123", "TestPlayer");
        testPlayer.setX(400);
        testPlayer.setY(300);
        testPlayer.setAngle(45);
        testPlayer.setScore(5);

        String playerJson = JsonUtil.toJson(testPlayer);
        System.out.println("Player JSON:");
        System.out.println(playerJson);
        System.out.println();

        // Test Bullet JSON
        Bullet testBullet = new Bullet("bullet-1", "test-id-123", 450, 320, 5, 5);
        String bulletJson = JsonUtil.toJson(testBullet);
        System.out.println("Bullet JSON:");
        System.out.println(bulletJson);
        System.out.println();

        // Test PowerUp JSON
        PowerUp testPowerUp = new PowerUp("power-1", PowerUp.Type.SHIELD, 500, 400);
        String powerUpJson = JsonUtil.toJson(testPowerUp);
        System.out.println("PowerUp JSON:");
        System.out.println(powerUpJson);
        System.out.println();

        // Test Update Message
        String updateMsg = JsonUtil.createUpdateMessage(
                Arrays.asList(testPlayer),
                Arrays.asList(testBullet),
                Arrays.asList(testPowerUp));
        System.out.println("Update Message:");
        System.out.println(updateMsg);
        System.out.println();

        // Test Chat Message
        String chatMsg = JsonUtil.createChatMessage("TestPlayer: Hello World!");
        System.out.println("Chat Message:");
        System.out.println(chatMsg);
        System.out.println();

        System.out.println("All tests completed!");
    }
}
