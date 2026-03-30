package com.dungeon.system;

import com.dungeon.model.Entity;

public class CombatSystem {
    
    public static void fight(Entity attacker, Entity defender) {
        System.out.println("--- DEBUT DU COMBAT ---");
        
       while (attacker.isAlive() && defender.isAlive()) {
        attacker.attack(defender);
        if (defender.isAlive()){
            defender.attack(attacker);
        }
       }
        
        if (attacker.isAlive()) {
            System.out.println(attacker.getName() + " a gagné !");
        } else {
            System.out.println(defender.getName() + " a gagné...");
        }
    }
}