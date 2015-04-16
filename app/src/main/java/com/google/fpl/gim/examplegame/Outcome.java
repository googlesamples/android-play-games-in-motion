/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.fpl.gim.examplegame;

/**
 * Encapsulates the game data changes that should occur as a result of a certain Choice
 */
public class Outcome {
    private boolean mDepleteWeaponCharge;
    private boolean mIncrementNumEnemiesDefeated;

    public Outcome(boolean depleteWeaponCharge, boolean incrementNumEnemiesDefeated) {
        this.mDepleteWeaponCharge = depleteWeaponCharge;
        this.mIncrementNumEnemiesDefeated = incrementNumEnemiesDefeated;
    }

    public boolean weaponChargeDepleted() {
        return this.mDepleteWeaponCharge;
    }

    public boolean numEnemiesDefeatedIncremented() {
        return this.mIncrementNumEnemiesDefeated;
    }
}
