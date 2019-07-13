/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

public enum PlayerFileVariable implements IFileVariable {
    //header
    headerVersion("headerVersion", VariableType.Integer, FileBlockType.PlayerHeader),
    playerCharacterClass("playerCharacterClass", VariableType.String, FileBlockType.PlayerHeader),
    uniqueId("uniqueId", VariableType.UID, FileBlockType.PlayerHeader),
    streamData("streamData", VariableType.Stream, FileBlockType.PlayerHeader),
    playerClassTag("playerClassTag", VariableType.String, FileBlockType.PlayerHeader),
    playerVersion("playerVersion", VariableType.Integer, FileBlockType.PlayerHeader),
    playerLevel("playerLevel", VariableType.Integer, FileBlockType.PlayerHeader),
    controllerStreamed("controllerStreamed", VariableType.Integer, FileBlockType.PlayerHeader),
    description("description", VariableType.Stream, FileBlockType.PlayerHeader),

    playerTexture("playerTexture", VariableType.String, FileBlockType.PlayerMain),
    myPlayerName("myPlayerName", VariableType.StringUtf16le, FileBlockType.PlayerMain),
    isInMainQuest("isInMainQuest", VariableType.Integer, FileBlockType.PlayerMain),
    disableAutoPopV2("disableAutoPopV2", VariableType.Integer, FileBlockType.PlayerMain),
    numTutorialPagesV2("numTutorialPagesV2", VariableType.Integer, FileBlockType.PlayerMain),
    currentPageV2("currentPageV2", VariableType.Integer, FileBlockType.PlayerMain),
    teleportUIDsSize("teleportUIDsSize", VariableType.Integer, FileBlockType.PlayerMain),
    markerUIDsSize("markerUIDsSize", VariableType.Integer, FileBlockType.PlayerMain),
    respawnUIDsSize("respawnUIDsSize", VariableType.Integer, FileBlockType.PlayerMain),
    versionCheckRespawnInfo("versionCheckRespawnInfo", VariableType.Integer, FileBlockType.PlayerMain),
    versionCheckTeleportInfo("versionCheckTeleportInfo", VariableType.Integer, FileBlockType.PlayerMain),
    versionCheckMovementInfo("versionCheckMovementInfo", VariableType.Integer, FileBlockType.PlayerMain),
    compassState("compassState", VariableType.Integer, FileBlockType.PlayerMain),
    skillWindowShowHelp("skillWindowShowHelp", VariableType.Integer, FileBlockType.PlayerMain),
    alternateConfig("alternateConfig", VariableType.Integer, FileBlockType.PlayerMain),
    alternateConfigEnabled("alternateConfigEnabled", VariableType.Integer, FileBlockType.PlayerMain),
    itemsFoundOverLifetimeUniqueTotal("itemsFoundOverLifetimeUniqueTotal", VariableType.Integer, FileBlockType.PlayerMain),
    itemsFoundOverLifetimeRandomizedTotal("itemsFoundOverLifetimeRandomizedTotal", VariableType.Integer, FileBlockType.PlayerMain),
    hasBeenInGame("hasBeenInGame", VariableType.Integer, FileBlockType.PlayerMain),
    versionRespawnPoint("versionRespawnPoint", VariableType.Integer, FileBlockType.PlayerMain),
    money("money", VariableType.Integer, FileBlockType.PlayerMain),
    teleportUID("teleportUID", VariableType.UID, FileBlockType.PlayerMain),
    respawnUID("respawnUID", VariableType.UID, FileBlockType.PlayerMain),
    markerUID("markerUID", VariableType.UID, FileBlockType.PlayerMain),
    strategicMovementRespawnPoint("strategicMovementRespawnPoint[i]", VariableType.UID, FileBlockType.PlayerMain),

    skillName("skillName", VariableType.String, FileBlockType.Body),//PlayerSkills + PlayerHotSlot
    skillActive("skillActive", VariableType.Integer, FileBlockType.PlayerSkills),
    skillLevel("skillLevel", VariableType.Integer, FileBlockType.PlayerSkills),
    skillEnabled("skillEnabled", VariableType.Integer, FileBlockType.PlayerSkills),
    skillSubLevel("skillSubLevel", VariableType.Integer, FileBlockType.PlayerSkills),
    skillTransition("skillTransition", VariableType.Integer, FileBlockType.PlayerSkills),
    max("max", VariableType.Integer, FileBlockType.PlayerSkills),
    masteriesAllowed("masteriesAllowed", VariableType.Integer, FileBlockType.PlayerSkills),
    skillReclamationPointsUsed("skillReclamationPointsUsed", VariableType.Integer, FileBlockType.PlayerSkills),

    defaultText("defaultText", VariableType.StringUtf16le, FileBlockType.Body),
    itemPositionsSavedAsGridCoords("itemPositionsSavedAsGridCoords", VariableType.Integer, FileBlockType.PlayerInventory),
    numberOfSacks("numberOfSacks", VariableType.Integer, FileBlockType.PlayerInventory),
    currentlyFocusedSackNumber("currentlyFocusedSackNumber", VariableType.Integer, FileBlockType.PlayerInventory),
    currentlySelectedSackNumber("currentlySelectedSackNumber", VariableType.Integer, FileBlockType.PlayerInventory),
    tempBool("tempBool", VariableType.Integer, FileBlockType.PlayerInventory),
    size("size", VariableType.Integer, FileBlockType.PlayerInventory),
    seed("seed", VariableType.Integer, FileBlockType.PlayerInventory),
    var1("var1", VariableType.Integer, FileBlockType.PlayerInventory),
    var2("var2", VariableType.Integer, FileBlockType.PlayerInventory),
    pointX("pointX", VariableType.Integer, FileBlockType.PlayerInventory),
    pointY("pointY", VariableType.Integer, FileBlockType.PlayerInventory),
    baseName("baseName", VariableType.String, FileBlockType.PlayerInventory),
    prefixName("prefixName", VariableType.String, FileBlockType.PlayerInventory),
    suffixName("suffixName", VariableType.String, FileBlockType.PlayerInventory),
    relicName("relicName", VariableType.String, FileBlockType.PlayerInventory),
    relicBonus("relicBonus", VariableType.String, FileBlockType.PlayerInventory),
    relicName2("relicName2", VariableType.String, FileBlockType.PlayerInventory),
    relicBonus2("relicBonus2", VariableType.String, FileBlockType.PlayerInventory),
    useAlternate("useAlternate", VariableType.Integer, FileBlockType.PlayerInventory),
    equipmentCtrlIOStreamVersion("equipmentCtrlIOStreamVersion", VariableType.Integer, FileBlockType.PlayerInventory),
    storedType("storedType", VariableType.Integer, FileBlockType.PlayerHotSlot),
    itemAttached("itemAttached", VariableType.Integer, FileBlockType.PlayerHotSlot),
    alternate("alternate", VariableType.Integer, FileBlockType.PlayerHotSlot),
    isItemSkill("isItemSkill", VariableType.Integer, FileBlockType.PlayerHotSlot),
    itemName("itemName", VariableType.Integer, FileBlockType.PlayerHotSlot),
    scrollName("scrollName", VariableType.String, FileBlockType.PlayerHotSlot),
    bitmapUpName("bitmapUpName", VariableType.String, FileBlockType.PlayerHotSlot),
    bitmapDownName("bitmapDownName", VariableType.String, FileBlockType.PlayerHotSlot),
    storedDefaultType("storedDefaultType", VariableType.Integer, FileBlockType.PlayerHotSlot),

    equipmentSelection("equipmentSelection", VariableType.Integer, FileBlockType.PlayerUiSkill),
    skillActive1("skillActive1", VariableType.Integer, FileBlockType.PlayerUiSkill),
    skillActive2("skillActive2", VariableType.Integer, FileBlockType.PlayerUiSkill),
    skillActive3("skillActive3", VariableType.Integer, FileBlockType.PlayerUiSkill),
    skillActive4("skillActive4", VariableType.Integer, FileBlockType.PlayerUiSkill),
    skillActive5("skillActive5", VariableType.Integer, FileBlockType.PlayerUiSkill),
    primarySkill1("primarySkill11", VariableType.Integer, FileBlockType.PlayerUiSkill),
    primarySkill2("primarySkill12", VariableType.Integer, FileBlockType.PlayerUiSkill),
    primarySkill3("primarySkill13", VariableType.Integer, FileBlockType.PlayerUiSkill),
    primarySkill4("primarySkill14", VariableType.Integer, FileBlockType.PlayerUiSkill),
    primarySkill5("primarySkill15", VariableType.Integer, FileBlockType.PlayerUiSkill),
    secondarySkill("secondarySkill", VariableType.Integer, FileBlockType.PlayerUiSkill),
    secondarySkill1("secondarySkill11", VariableType.Integer, FileBlockType.PlayerUiSkill),
    secondarySkill2("secondarySkill12", VariableType.Integer, FileBlockType.PlayerUiSkill),
    secondarySkill3("secondarySkill13", VariableType.Integer, FileBlockType.PlayerUiSkill),
    secondarySkill4("secondarySkill14", VariableType.Integer, FileBlockType.PlayerUiSkill),
    secondarySkill5("secondarySkill15", VariableType.Integer, FileBlockType.PlayerUiSkill),
    skillWindowSelection("skillWindowSelection", VariableType.Integer, FileBlockType.PlayerUiSkill),
    skillSettingValid("skillSettingValid", VariableType.Integer, FileBlockType.PlayerUiSkill),

    modifierPoints("modifierPoints", VariableType.Integer, FileBlockType.PlayerLevelPoints),
    skillPoints("skillPoints", VariableType.Integer, FileBlockType.PlayerLevelPoints),
    currentStats_experiencePoints("currentStats.experiencePoints", VariableType.Integer, FileBlockType.PlayerLevelPoints),
    currentStats_charLevel("currentStats.charLevel", VariableType.Integer, FileBlockType.PlayerLevelPoints),

    playTimeInSeconds("playTimeInSeconds", VariableType.Integer, FileBlockType.PlayerStats),
    greatestMonsterKilledName("greatestMonsterKilledName", VariableType.StringUtf16le, FileBlockType.PlayerStats),
    numberOfDeaths("numberOfDeaths", VariableType.Integer, FileBlockType.PlayerStats),
    numberOfKills("numberOfKills", VariableType.Integer, FileBlockType.PlayerStats),
    experienceFromKills("experienceFromKills", VariableType.Integer, FileBlockType.PlayerStats),
    healthPotionsUsed("healthPotionsUsed", VariableType.Integer, FileBlockType.PlayerStats),
    manaPotionsUsed("manaPotionsUsed", VariableType.Integer, FileBlockType.PlayerStats),
    maxLevel("maxLevel", VariableType.Integer, FileBlockType.PlayerStats),
    numHitsReceived("numHitsReceived", VariableType.Integer, FileBlockType.PlayerStats),
    numHitsInflicted("numHitsInflicted", VariableType.Integer, FileBlockType.PlayerStats),
    greatestDamageInflicted("greatestDamageInflicted", VariableType.Integer, FileBlockType.PlayerStats),
    greatestMonsterKilledLevel("(*greatestMonsterKilledLevel)[i]", VariableType.Integer, FileBlockType.PlayerStats),
    greatestMonsterKilledLifeAndMana("(*greatestMonsterKilledLifeAndMana)[i]", VariableType.Integer, FileBlockType.PlayerStats),
    criticalHitsInflicted("criticalHitsInflicted", VariableType.Integer, FileBlockType.PlayerStats),
    criticalHitsReceived("criticalHitsReceived", VariableType.Integer, FileBlockType.PlayerStats),

    //repeated variables with different types, should be named (name)__(blockname)
    temp("temp", VariableType.Unknown, FileBlockType.Multiple),
    temp__PlayerAttributes("temp", VariableType.Float, FileBlockType.PlayerAttributes),
    temp__PlayerMain("temp", VariableType.Integer, FileBlockType.PlayerMain),
    ;

    private final FileBlockType location;
    private final String var;
    private final VariableType type;

    PlayerFileVariable(String variable, VariableType type, FileBlockType location) {
        this.var = variable;
        this.type = type;
        this.location = location;
    }

    @Override
    public String var() {
        return var;
    }

    @Override
    public VariableType type() {
        return type;
    }

    @Override
    public FileBlockType location() {
        return location;
    }
}
