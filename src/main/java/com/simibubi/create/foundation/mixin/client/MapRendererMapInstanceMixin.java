package com.simibubi.create.foundation.mixin.client;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.map.CustomRenderedMapDecoration;

import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// fabric: we have an AW for it, and compiler complains if specified by string
@Mixin(MapRenderer.MapInstance.class)
public class MapRendererMapInstanceMixin {
	@Shadow
	private MapItemSavedData data;

	// fabric: completely redone

	@ModifyExpressionValue(
		method = "draw",
		at = @At(
				value = "INVOKE",
				target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"
		)
	)
	private Iterator<MapDecoration> wrapIterator(Iterator<MapDecoration> original) {
		// skip rendering custom ones in the main loop
		return Iterators.filter(original, decoration -> !(decoration instanceof CustomRenderedMapDecoration));
	}

	@Inject(method = "draw", at = @At("TAIL"))
	private void renderCustomDecorations(PoseStack poseStack, MultiBufferSource bufferSource, boolean active,
										 int packedLight, CallbackInfo ci, @Local(ordinal = 3) int index) { // ignore error, works
		// render custom ones in second loop
		for (MapDecoration decoration : this.data.getDecorations()) {
			if (decoration instanceof CustomRenderedMapDecoration renderer) {
				renderer.render(poseStack, bufferSource, active, packedLight, data, index);
			}
		}
	}
}
