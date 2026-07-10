/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.advancements;

import vazkii.botania.mixin.CriteriaTriggersAccessor;

public class BotaniaCriteriaTriggers {
	public static void init() {
		CriteriaTriggersAccessor.botania_register(AlfheimPortalTrigger.ID.toString(), AlfheimPortalTrigger.INSTANCE);
		CriteriaTriggersAccessor.botania_register(CorporeaRequestTrigger.ID.toString(), CorporeaRequestTrigger.INSTANCE);
		CriteriaTriggersAccessor.botania_register(GaiaGuardianNoArmorTrigger.ID.toString(), GaiaGuardianNoArmorTrigger.INSTANCE);
		CriteriaTriggersAccessor.botania_register(RelicBindTrigger.ID.toString(), RelicBindTrigger.INSTANCE);
		CriteriaTriggersAccessor.botania_register(UseItemSuccessTrigger.ID.toString(), UseItemSuccessTrigger.INSTANCE);
		CriteriaTriggersAccessor.botania_register(ManaBlasterTrigger.ID.toString(), ManaBlasterTrigger.INSTANCE);
		CriteriaTriggersAccessor.botania_register(LokiPlaceTrigger.ID.toString(), LokiPlaceTrigger.INSTANCE);
		CriteriaTriggersAccessor.botania_register(AlfheimPortalBreadTrigger.ID.toString(), AlfheimPortalBreadTrigger.INSTANCE);
	}
}
