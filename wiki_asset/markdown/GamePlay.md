# Upgrade Your Weapon
After put your weapon in Mod Operation Table, it will reveal more information of your weapon: 
* Damage Compose
* Weapon Level and Exp to upgrade
* Mod Capability
* Mod Slots

When you gain exp with revealed weapon or chest in your slots including 
`mainhand`, `offhand` and `chest`, 
The exp will be evenly distributed among these items. 
These overflowing exp will gained by yourself.

It is worth noting that (as an example) when your have revealed weapons in your mainhand and offhand, 
your weapon in one of your hands can only gain half of exp you get, although the 
weapon in your offhand is already the Max Level! 
This is as the same as Warframe.

# Build Your Weapon
To equip Mods in your weapon, you must have enough Capability.

Every Mod has a basic Capability Cost, each level up will add a more cost.

You are hard to equip all the eight Mods in your weapon even it is the Max Level (60 Capability).

To reduce the cost of Capability, you can Polarity your weapon's Mod Slot.

After you can equip enough Mods, you are supposed to make a unique build for your weapon.

## Composite Elements
You have noticed that there are four elements in Damage Types, each two of them can combine to a composite element.
* Cold + Toxin = Viral
* Cold + Electricity = Magnetic
* Cold + Heat = Blast
* Toxin + Electricity = Corrosive
* Toxin + Heat = Gas
* Electricity + Heat = Radiation

### Weapons With Element
Some weapons have their default element.

In this case, the default element is stable 
and generally does not combine with the elements added by Mods.

According to this rule, you are capable of making more possibilities!


# Polarity
Without any polarities in your weapon, you are hard to equip 8 Mods!

To make or change a polarity, you need to spend a Forma and your weapon must be the Max Level.

The exp to upgrade your weapon will be increased due to the number of you modify polarity.

## Function
* When you Mod put in the Mod Slot with the same polarity, it just cost half of its original Capability Cost.
* But if it is conflicting, it will cost an additional quarter.

## Method
* Item in Slot 1
* Forma in Slot 2
* Item has reached Max Level
* Choose a Mod Slot
* Click Polarity bottom

<img src="https://raw.githubusercontent.com/WaitMyDawn/yagens_attributes/main/wiki_asset/gameplay/polarity.png" alt="polarity method">

# Upgrade Your Mod
To upgrade your Mod, you need to spend Mod Essence in Mod Operation

## Method
* Item in Slot 1
* Choose a Mod Slot
* Click Upgrade bottom

<img src="https://raw.githubusercontent.com/WaitMyDawn/yagens_attributes/main/wiki_asset/gameplay/upgrade.png" alt="upgrade">

This will cost Mod Essence in your inventory, 
and it costs according to Mod Rarity 
and Moe Level of your Mod
* $cost = (modRarityValue+1)\times 2^{modLevel-1}$

For instance, to upgrade a `Edge Disc` Mod, 
`Mod Rarity = Legendary` and `Mod Level = 7`, it costs: 
* $ cost=(3+1)\times 2^{7-1}=256 $

# Cycle
Cycle is the indispensable way to obtain Riven Mod that suits you best

To make Cycle easily, you just need 4 Kuva each time.

## Method
* Item in Slot 1
* Riven Mod in Slot 2
* Choose any one of Mod Slots
* Click Cycle bottom

Don't forget to check the cycled UniqueInfos!

<img src="https://raw.githubusercontent.com/WaitMyDawn/yagens_attributes/main/wiki_asset/gameplay/cycle.png" alt="cycle">

# Recycle
To gain more Mod Essence, you can Recycle your needless Mods in Mod Recycle Table.

The quantity of recycling depends on your Mod Rarity and Mod Level, exactly: 
* $quantity = (modRarityValue+1)\times 2^{modLevel-3}$

or as you can treat as

* $quantity = \dfrac{nextLevelUpgradeNeedQuantity}{4}$

## Method
* Mods in Left Slots
* Click Recycle bottom (in red rectangle)

<img src="https://raw.githubusercontent.com/WaitMyDawn/yagens_attributes/main/wiki_asset/gameplay/recycle_and_transform.png" alt="recycle and transform">

# Transform
To get a new Mod without an adventure, you can cost 4 Mods in Mod Recycle Table to Transform a New Mod.

The result of Transform depends on your Mod Rarity and quantity.

The accurate rules are: 
* Each Mod Rarity has a wight: $weight = 4-modRarityValue$
* Each Transform needs 4 Mods
* If you want to Transform an Unknown Riven, you need to spend 4 Riven Rarity Mods
* You can't Transform Warframe Rarity Mods.

To make it clearly, there are several examples: 

Let $Possibility(Common,Uncommon,Rare,Legendary)$ shows the possibility of Mod Rarity of result Mod.
* $ 1Common+1Uncommon+1Rare+1Legendary=Possibility(0.4,0.3,0.2,0.1) $
* $ 2Common+2Uncommon=Possibility(\dfrac{4}{7},\dfrac{3}{7},0,0) $
* $ 4Common=Possibility(1,0,0,0) $
* $ 4Riven = 1 Unknown Riven$

## Method
* Mods in Left Slots, it will take the first four Mods
* Click Transform bottom (in blue rectangle)

<img src="https://raw.githubusercontent.com/WaitMyDawn/yagens_attributes/main/wiki_asset/gameplay/recycle_and_transform.png" alt="recycle and transform">

# Spyglass
To know Health Material Type of the entity, you can use Spyglass to get details.

This also shows the bonus factors of Damage Types.

<img src="https://raw.githubusercontent.com/WaitMyDawn/yagens_attributes/main/wiki_asset/gameplay/spyglass.png" alt="spyglass">
