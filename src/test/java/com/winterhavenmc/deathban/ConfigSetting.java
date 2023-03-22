/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathban;


public enum ConfigSetting {

    LANGUAGE("en-US"),
    ENABLED_WORLDS("[]"),
    DISABLED_WORLDS("[]"),
    BAN_IP("false"),
    BAN_TIME("1440"),
    KICK_DELAY("1"),
    LOG_BANS("true"),
    SOUND_EFFECTS("true"),
    BAN_REASON("SavageDeathBan")
    ;

    private final String value;

    ConfigSetting(String value) {
        this.value = value;
    }

    public String getKey() {
        return this.name().toLowerCase().replace('_', '-');
    }
    public String getValue() {
        return this.value;
    }

}
