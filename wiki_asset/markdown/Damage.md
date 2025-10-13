Based on the damage system of Warframe, this mod takes many changes to Minecraft!
# General Damage
Now, different weapons have different damage compose and different entities have different health material. The final damage is the result of the combined effect of damage type and health material type.

## Critical
Based on your `Critical Chance` and `Critical Damage` of your weapon, you can make critical hit to entity.

### Overflowing Critical Chance
When your `Critical Chance` greater than 100%, you can make a higher level critical hit
* 0&ndash;100%: you have chance to make <font color="#FFFF00">Critical Hit</font> (0xFFFF00, effected by your `Critical Damage`)
* 100&ndash;200%: you have chance to make <font color="#FF4500">Critical Hit</font> (0xFF4500, your `Critical Damage` will add 1x)
* 200&ndash;300%: you have chance to make <font color="#FF0000">Critical Hit</font> (0xFF0000, your `Critical Damage` will add 2x)
* 300%+: you have chance to make <font color="#FF0000">Critical Hit</font> with `!` right behind (0xFF0000, your `Critical Damage` will add based on your Critical Level. Each 100% add a Level and when above 300% will add `!`)


## Status

### Slash
Causing continuous `Slash Damage` which bypass armor, effects, enchantments and resistance.

### Puncture
The damage of entity with `Puncture Status` will be reduced.
* Amplifier=0 will reduce 20%
* For each additional level, it reduces more 10%
* The max reduction is 80%

### Impact
Make the target temporarily immobile and unable to attack

### Cold
Reducing `movement_speed` and `attack_speed`.
* Amplifier=0 will reduce 30%
* For each additional level, it reduces more 6%
* The max reduction is 60%

Increasing your `Critical Damage` when you attack entity with `Cold Status`
* Amplifier=0 will increase 30%
* For each additional level, it increases more 10%
* The max increasing amount is 80%

### Heat
Causing continuous `Heat Damage` and reducing `armor`
* Amplifier=0 will reduce 12.5%
* For each additional level, it reduces more 12.5%
* The max reduction is 50%

### Electricity
Causing continuous `Electricity Damage` and transmits it to the entities within the range of entity with `Electricity Status`

### Toxin
Causing continuous `Toxin Damage` which bypass enchantments

### Corrosive
Reducing `armor` and `armor_toughness`
* Amplifier=0 will reduce 40%
* 40&ndash;60%: For each additional level, it reduces more 10%
* 60&ndash;90%: For each additional level, it reduces more 5%
* The max reduction is 90%

### Viral
Increasing your damage when you attack entity with `Viral Status`
* Amplifier=0 will increase 75%
* For each additional level, it increases more 25%
* The max increasing amount is 300%

### Gas
Causing continuous `Gas Damage`.

`Gas Status` will apply to the entities in the range of entity who you status.

### Blast
Make a blast based on your damage!

### Magnetic
Apply darkness and blindness to the entity at the same time

### Radiation
Force the entity to attack another one

## Health Material
There are four kinds of health material types, including `flesh`(default), `bone`, `metal`, `arthropod`.

### Amplification Factor (default = 1)
#### Flesh
* Slash: 1.2
* Impact: 0.8
* Heat: 1.2
* Toxin: 1.2
* Viral: 1.6

#### Bone
* Slash: 0.9
* Puncture: 0.8
* Impact: 1.2
* Cold: 0.8
* Toxin: 1.2
* Corrosive: 1.4
* Magnetic: 1.2
* Viral: 0.8

#### Metal
* Toxin: 0.6
* Electricity: 1.2
* Gas: 0.6
* Corrosive: 1.5
* Magnetic: 1.5
* Blast: 1.2
* Viral: 0.8

#### Metal
* Slash: 1.2
* Puncture: 0.8
* Heat: 1.2
* Viral: 1.2

## Bow Shoot
### Multishot
This mod add a new attribute `multishot`(default = 1), this will effect like Critical Chance.

For example:
* when your Multishot = 1.5, you have 50% chance to shoot 2 arrows.
* when your Multishot = 2.7, you shoot 2 arrows at least, and have 70% chance to shoot 3 arrows.

The added arrows are as the same as the original one

### Spread
A new attribute `shoot_spread` will influence the diffusion of your arrows.

# Armor
To adapt to the high damage of this mod, the effectiveness of your armors has been changed.
Let `A = armor`, `T = armor_toughness`. Then
* $X = A \times  (T+4)$
* basic modifier factor: $BMF = \dfrac{40}{X+40}$
* basic effectiveness: $BE = 1 - BMF$
* equivalent health: $EH=\dfrac{20}{BMF}$

Same as vanilla, a high damage will reduce the effectiveness of your defense
* after damage effectiveness: $ADE = \max(\dfrac{BE}{4}, BE - \dfrac{damage}{EH + \dfrac{EH}{8}\times T})$

In the end, $ADE$ will be sent to vanilla enchantment modifier function and get final effectiveness $(FE)$
* final modifier factor: $FMF = 1 - FE$
* final damage: $FD = damage \times FMF$