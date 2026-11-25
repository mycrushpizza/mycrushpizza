package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycrushpizza.`in`.R
import com.example.data.model.HajipurLocations
import com.example.data.model.PizzaOrder
import com.example.data.model.formatInr
import com.example.ui.theme.CrushBlack
import com.example.ui.theme.CrushCardSurface
import com.example.ui.theme.CrushMutedText
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushRedDark
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import com.example.ui.theme.VegGreenBright

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

const val OFFICIAL_STORE_PHONE_DISPLAY = "+91 95238 41563"
const val OFFICIAL_STORE_PHONE_DIAL = "+919523841563"
const val OFFICIAL_STORE_WHATSAPP_NUMBER = "919523841563"

/**
 * Calibrated Studio Brightness, Contrast & Saturation ColorFilter so every food photo
 * across the menu, combos, medallions, and cart appears crystal clear, bright, and appetizing.
 */
val BrightClearFoodColorFilter: ColorFilter = run {
    val saturationMatrix = ColorMatrix().apply {
        setToSaturation(1.18f)
    }
    val c = 1.14f // Crisp contrast boost
    val b = 15f   // Bright studio exposure lift
    val contrastBrightnessMatrix = ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, b,
            0f, c, 0f, 0f, b,
            0f, 0f, c, 0f, b,
            0f, 0f, 0f, 1f, 0f
        )
    )
    saturationMatrix.timesAssign(contrastBrightnessMatrix)
    ColorFilter.colorMatrix(saturationMatrix)
}

/**
 * Online-first Food Image composable powered by Coil AsyncImage:
 * - Renders every food photo with `BrightClearFoodColorFilter` + `FilterQuality.High` on a bright white studio backing
 *   so all food photos look vivid, clear, and well-lit.
 */
@Composable
fun OnlineFoodImage(
    imageKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    imageUrl: String = "",
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val fallbackRes = resolveFoodDrawable(imageKey)
    val cleanUrl = imageUrl.trim()

    if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(cleanUrl)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .fallback(fallbackRes)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            colorFilter = BrightClearFoodColorFilter,
            filterQuality = FilterQuality.High,
            modifier = modifier.background(Color.White)
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            colorFilter = BrightClearFoodColorFilter,
            modifier = modifier.background(Color.White)
        )
    }
}

@DrawableRes
fun resolveFoodDrawable(imageKey: String): Int {
    return when (imageKey.lowercase()) {
        "hero_pizza" -> R.drawable.img_country_feast_golden_1794920658016
        "cheese_pizza" -> R.drawable.img_double_cheese_golden_1794920749161
        "burger_pasta" -> R.drawable.img_cheese_burger_clean
        "feast_combo" -> R.drawable.img_feast_combo_clean
        "peppy_paneer" -> R.drawable.img_peppy_paneer_golden_1794920700880
        "kulhad_pizza" -> R.drawable.img_veg_kulhad_pizza_clean
        "grilled_sandwich" -> R.drawable.img_cheese_corn_sandwich_clean
        "zingy_garlic" -> R.drawable.img_stuffed_garlic_bread_fresh_1795149481332
        "shakes_lava" -> R.drawable.img_oreo_shake_clean
        "paneer_handi" -> R.drawable.img_paneer_handi_distinct_1794922388436
        "chilli_noodles" -> R.drawable.img_paneer_noodles_clean
        "dum_biryani" -> R.drawable.img_dum_biryani_clean
        "cheese_maggi" -> R.drawable.img_cheese_maggi_clean
        "tandoori_kabab" -> R.drawable.img_chicken_tikka_fresh_1795088658770
        "pizza_teaser" -> R.drawable.img_tomato_golden_1794920699068
        "crush_special" -> R.drawable.img_crush_special_golden_1794920794751
        "farmhouse_pizza" -> R.drawable.img_farmhouse_golden_1794920656262
        "margherita_pizza" -> R.drawable.img_margherita_pizza_big_1794921829845
        "tandoori_burger" -> R.drawable.img_tandoori_paneer_burger_clean
        "paneer_wrap" -> R.drawable.img_paneer_wrap_clean
        "zingy_parcel" -> R.drawable.img_zingy_parcel_clean
        "white_pasta" -> R.drawable.img_white_pasta_clean
        "cold_coffee" -> R.drawable.img_cold_coffee_clean
        "gulab_jamun" -> R.drawable.img_kulhad_lassi_fresh_1795149482977
        "spicy_tango_pizza" -> R.drawable.img_spicy_tango_golden_1794920793409
        "tandoori_paneer_pizza" -> R.drawable.img_tandoori_paneer_golden_1794920659450
        "onion_capsicum_pizza" -> R.drawable.img_onion_capsicum_golden_1794920700945
        "jalapeno_olive_pizza" -> R.drawable.img_jalapeno_olive_clean
        "paprika_pineapple_pizza" -> R.drawable.img_paprika_pineapple_golden_1794920797017
        "peri_peri_fries" -> R.drawable.img_peri_peri_fries_distinct_1794965699841
        "red_sauce_pasta" -> R.drawable.img_red_pasta_clean
        "mixed_sauce_pasta" -> R.drawable.img_mixed_pasta_clean
        "kitkat_shake" -> R.drawable.img_kitkat_shake_clean
        "butterscotch_pastry" -> R.drawable.img_butterscotch_pastry_clean
        "paneer_pakora" -> R.drawable.img_paneer_pakora_fresh_1795010403007
        "chicken_lollypop" -> R.drawable.img_chicken_lollypop_distinct_1794967224379
        "shahi_paneer" -> R.drawable.img_shahi_paneer_fresh_1795084318961
        "chicken_handi" -> R.drawable.img_chicken_handi_clean
        "garlic_naan_kulcha" -> R.drawable.img_masala_kulcha_clean
        "manchow_soup_salad" -> R.drawable.img_veg_manchow_soup_fresh_1795150231453
        "onion_paneer_pizza" -> R.drawable.img_onion_paneer_golden_1794920743652
        "capsicum_corn_pizza" -> R.drawable.img_capsicum_golden_1794920661779
        "chicken_burger_sandwich" -> R.drawable.img_veg_grilled_sandwich_fresh_1795089242229
        "chicken_burger_closeup" -> R.drawable.img_chicken_burger_clean
        "chicken_grilled_sandwich_closeup" -> R.drawable.img_chicken_grilled_sandwich_clean
        "nacho_garlic_bread" -> R.drawable.img_garlic_naan_fresh_1795010746665
        "nacho_chips_closeup" -> R.drawable.img_nacho_chips_clean
        "classic_garlic_bread_closeup" -> R.drawable.img_garlic_bread_clean
        "veg_pakora_spring_roll" -> R.drawable.img_mushroom_pakora_fresh_1795010405296
        "spring_roll_closeup" -> R.drawable.img_spring_roll_clean
        "onion_veg_pakora_closeup" -> R.drawable.img_onion_pakora_clean
        "chilli_potato_manchurian" -> R.drawable.img_paneer_65_masala_clean
        "honey_chilli_potato_closeup" -> R.drawable.img_honey_chilli_potato_clean
        "gobi_veg_manchurian_closeup" -> R.drawable.img_veg_manchurian_fresh_1795090312016
        "dal_makhni_jeera_rice" -> R.drawable.img_dal_tarka_fresh_1795010720293
        "dal_makhni_closeup" -> R.drawable.img_dal_makhni_clean
        "jeera_fried_rice_closeup" -> R.drawable.img_jeera_rice_fresh_1795010724974
        "lachha_aloo_paratha_closeup" -> R.drawable.img_lachha_paratha_clean
        "mutton_rogan_chicken_dehati" -> R.drawable.img_chicken_rara_fresh_1795089135777
        "mutton_rara_lal_maas_closeup" -> R.drawable.img_mutton_rara_clean
        "chicken_dehati_closeup" -> R.drawable.img_chicken_dehati_clean
        "malai_kofta_kaju_korma" -> R.drawable.img_paneer_lawabdar_fresh_1795010855320
        "kaju_korma_closeup" -> R.drawable.img_kaju_korma_clean
        "malai_kofta_closeup" -> R.drawable.img_malai_kofta_clean
        "mushroom_masala_mix_veg" -> R.drawable.img_paneer_masala_fresh_1795010854677
        "mushroom_do_pyaza_closeup" -> R.drawable.img_mushroom_do_pyaza_distinct_1794967369598
        "mix_veg_curry_closeup" -> R.drawable.img_mix_veg_clean
        "schezwan_chicken_chowmein" -> R.drawable.img_chilly_garlic_chowmin_fresh_1795010798530
        "chicken_chowmein_closeup" -> R.drawable.img_chicken_chowmein_fresh_1795089162359
        "chicken_manchurian_closeup" -> R.drawable.img_chicken_manchurian_fresh_1795089158911
        "choco_lava_cake_solo" -> R.drawable.food_choco_lava_cake_solo
        "black_forest_pastry_solo" -> R.drawable.food_black_forest_pastry_solo
        "gulab_jamun_solo" -> R.drawable.img_gulab_jamun_fresh_1795149480224
        "butter_naan_solo" -> R.drawable.img_butter_naan_fresh_1795010745917
        "green_salad_raita_solo" -> R.drawable.img_green_salad_fresh_1795089307818
        "chicken_tandoori_solo" -> R.drawable.food_chicken_tandoori_solo
        "chicken_leg_biryani_solo" -> R.drawable.food_chicken_leg_biryani_solo
        "veg_masala_maggi_solo" -> R.drawable.food_veg_masala_maggi_solo
        "choco_mousse_cone_solo" -> R.drawable.food_choco_mousse_cone_solo
        "corn_pizza_solo" -> R.drawable.img_corn_golden_1794920663967
        "onion_pizza_solo" -> R.drawable.img_onion_golden_1794920700952
        "tomato_corn_pizza_solo" -> R.drawable.img_tomato_corn_golden_1794920700223
        "capsicum_paneer_pizza_solo" -> R.drawable.img_capsicum_paneer_golden_1794920745052
        "veg_pakora_solo" -> R.drawable.img_veg_pakora_fresh_1795010406057
        "aloo_pakora_solo" -> R.drawable.img_aloo_pakora_fresh_1795010402896
        "chicken_pakora_solo" -> R.drawable.img_chicken_pakora_distinct_1794967224456
        "chicken_seek_kabab_solo" -> R.drawable.food_chicken_seek_kabab_solo
        "chicken_boti_kabab_solo" -> R.drawable.img_chicken_boti_kabab_fresh_1795088660330
        "chicken_angara_kabab_solo" -> R.drawable.img_chicken_angara_kabab_distinct_1794967197585
        "chicken_malai_tikka_solo" -> R.drawable.food_chicken_malai_tikka_solo
        "chicken_reshmi_kabab_solo" -> R.drawable.img_chicken_reshmi_kabab_fresh_1795089199405
        "chicken_tash_fry_solo" -> R.drawable.img_chicken_tash_fry_fresh_1795089198045
        "chicken_hara_bhara_kabab_solo" -> R.drawable.food_chicken_hara_bhara_kabab_solo
        "paneer_hong_kong_solo" -> R.drawable.food_paneer_hong_kong_solo
        "finger_chips_solo" -> R.drawable.food_finger_chips_solo
        "crispy_baby_corn_solo" -> R.drawable.food_crispy_baby_corn_solo
        "chilly_baby_corn_solo" -> R.drawable.food_chilly_baby_corn_solo
        "chicken_spring_roll_solo" -> R.drawable.img_chicken_spring_roll_fresh_1795089198137
        "chicken_chilly_bone_solo" -> R.drawable.img_chilly_chicken_bone_1794839059505
        "chicken_chilly_boneless_solo" -> R.drawable.img_chilly_chicken_boneless_1794839061311
        "chicken_crispy_solo" -> R.drawable.img_chicken_crispy_fresh_1795010641028
        "veg_burger_solo" -> R.drawable.img_veg_burger_fresh_1795089239580
        "veg_sandwich_solo" -> R.drawable.img_veg_sandwich_fresh_1795089236736
        "french_fries_solo" -> R.drawable.img_classic_french_fries_distinct_1794965699748
        "boiled_egg_solo" -> R.drawable.food_boiled_egg_solo
        "egg_omlet_double_solo" -> R.drawable.food_egg_omlet_double_solo
        "fry_egg_solo" -> R.drawable.img_fry_egg_fresh_1795090796126
        "omlet_curry_solo" -> R.drawable.img_omlet_curry_fresh_1795010881508
        "omlet_burgi_solo" -> R.drawable.food_omlet_burgi_solo
        "maggi_plain_solo" -> R.drawable.food_maggi_plain_solo
        "gobi_manchurian_solo" -> R.drawable.img_gobi_manchurian_fresh_1795089246599
        "paneer_manchurian_solo" -> R.drawable.img_paneer_manchurian_clean
        "paneer_chilly_solo" -> R.drawable.img_paneer_chilly_clean
        "mushroom_chilly_solo" -> R.drawable.img_mushroom_chilly_clean
        "veg_chowmin_solo" -> R.drawable.img_veg_chowmin_fresh_1795010796215
        "jaipuri_chowmin_solo" -> R.drawable.img_jaipuri_chowmin_fresh_1795010826944
        "paneer_gravy_noodles_solo" -> R.drawable.img_paneer_gravy_noodles_distinct_1794967424126
        "veg_manchurian_chowmin_solo" -> R.drawable.img_veg_manchurian_chowmin_fresh_1795010827631
        "chicken_schezwan_chowmin_solo" -> R.drawable.img_chicken_schezwan_chowmin_fresh_1795010798845
        "chicken_singapuri_chowmin_solo" -> R.drawable.img_chicken_singapuri_chowmin_fresh_1795010798924
        "chicken_egg_chowmin_solo" -> R.drawable.img_chicken_egg_chowmin_fresh_1795010801004
        "chocolate_pastry_solo" -> R.drawable.food_chocolate_pastry_solo
        "white_forest_pastry_solo" -> R.drawable.food_white_forest_pastry_solo
        "banana_shake_solo" -> R.drawable.food_banana_shake_solo
        "chocolate_shake_solo" -> R.drawable.food_chocolate_shake_solo
        "ice_cream_cone_solo" -> R.drawable.food_ice_cream_cone_solo
        "butterscotch_muse_solo" -> R.drawable.food_butterscotch_muse_solo
        "white_sponges_solo" -> R.drawable.food_white_sponges_solo
        "mineral_water_solo" -> R.drawable.img_mineral_water_clean
        "coldrink_glass_solo" -> R.drawable.img_coldrink_glass_clean
        "coldrink_can_solo" -> R.drawable.img_coldrink_can_clean
        "veg_clear_soup_solo" -> R.drawable.img_veg_clear_soup_fresh_1795089309661
        "veg_hot_sour_soup_solo" -> R.drawable.food_veg_hot_sour_soup_solo
        "lemon_coriander_soup_solo" -> R.drawable.food_lemon_coriander_soup_solo
        "tomato_soup_solo" -> R.drawable.img_tomato_soup_distinct_1794453741838
        "tomato_creamy_soup_solo" -> R.drawable.food_tomato_creamy_soup_solo
        "mushroom_pepper_soup_solo" -> R.drawable.img_mushroom_pepper_soup_clean
        "chicken_clear_soup_solo" -> R.drawable.img_chicken_clear_soup_fresh_1795089310476
        "chicken_hot_sour_soup_solo" -> R.drawable.img_chicken_hot_sour_soup_fresh_1795089311850
        "chicken_manchow_soup_solo" -> R.drawable.img_chicken_manchow_soup_fresh_1795089314483
        "chicken_sweet_corn_soup_solo" -> R.drawable.food_chicken_sweet_corn_soup_solo
        "paneer_changezi_solo" -> R.drawable.img_paneer_changezi_fresh_1795010856714
        "paneer_kadai_solo" -> R.drawable.img_paneer_kadai_distinct_1794922389224
        "paneer_punjabi_solo" -> R.drawable.img_paneer_punjabi_distinct_1794967371248
        "paneer_do_pyaza_solo" -> R.drawable.img_paneer_do_pyaza_fresh_1795010854722
        "paneer_kolhapuri_solo" -> R.drawable.img_paneer_kolhapuri_clean
        "paneer_malai_kofta_solo" -> R.drawable.img_paneer_malai_kofta_fresh_1795010856428
        "paneer_tikka_masala_solo" -> R.drawable.img_paneer_tikka_masala_clean
        "mushroom_masala_solo" -> R.drawable.img_mushroom_masala_distinct_1794967198258
        "chicken_do_pyaza_solo" -> R.drawable.img_chicken_do_pyaza_fresh_1795089133471
        "chicken_curry_masala_solo" -> R.drawable.img_chicken_curry_masala_fresh_1795089134503
        "chicken_kadai_solo" -> R.drawable.img_chicken_kadai_fresh_1795089133388
        "chicken_bharta_solo" -> R.drawable.img_chicken_bharta_distinct_1794967198452
        "chicken_kasha_solo" -> R.drawable.img_chicken_kasha_distinct_1794967420089
        "chicken_afgani_curry_solo" -> R.drawable.img_chicken_afgani_curry_fresh_1795010878456
        "chicken_murg_musallam_solo" -> R.drawable.img_chicken_murg_musallam_clean
        "chicken_65_lawabdar_solo" -> R.drawable.img_chicken_65_lawabdar_fresh_1795010641241
        "chicken_changezi_solo" -> R.drawable.img_chicken_changezi_fresh_1795089200590
        "chicken_schezwan_main_solo" -> R.drawable.img_chicken_schezwan_main_fresh_1795010828923
        "chicken_garlic_main_solo" -> R.drawable.img_chicken_garlic_main_fresh_1795010830399
        "chicken_mangolian_main_solo" -> R.drawable.img_chicken_mangolian_main_fresh_1795010830074
        "mutton_handi_solo" -> R.drawable.img_mutton_handi_distinct_1794967420838
        "mutton_kadai_solo" -> R.drawable.img_mutton_kadai_fresh_1795089201086
        "mutton_korma_spl_solo" -> R.drawable.img_mutton_korma_distinct_1794967423358
        "mutton_lal_maas_solo" -> R.drawable.img_mutton_lal_maas_fresh_1795089212201
        "steam_rice_solo" -> R.drawable.food_steam_rice_solo
        "veg_fried_rice_solo" -> R.drawable.food_veg_fried_rice_solo
        "paneer_fried_rice_solo" -> R.drawable.img_paneer_fried_rice_fresh_1795010719509
        "chilly_garlic_fried_rice_solo" -> R.drawable.img_chilly_garlic_fried_rice_fresh_1795010719219
        "mushroom_onion_fried_rice_solo" -> R.drawable.img_mushroom_onion_fried_rice_fresh_1795010773519
        "baby_corn_fried_rice_solo" -> R.drawable.img_baby_corn_fried_rice_fresh_1795010772610
        "chicken_fried_rice_solo" -> R.drawable.img_chicken_fried_rice_fresh_1795010774759
        "chicken_egg_fried_rice_solo" -> R.drawable.img_chicken_egg_fried_rice_fresh_1795010773605
        "green_pea_pulao_solo" -> R.drawable.food_green_pea_pulao_solo
        "sahi_pulao_solo" -> R.drawable.food_sahi_pulao_solo
        "kashmiri_pulao_solo" -> R.drawable.img_kashmiri_pulao_fresh_1795010776276
        "paneer_biryani_solo" -> R.drawable.food_paneer_biryani_solo
        "egg_biryani_solo" -> R.drawable.food_egg_biryani_solo
        "chicken_dum_kolkata_solo" -> R.drawable.food_chicken_dum_kolkata_solo
        "chef_spl_fried_rice_solo" -> R.drawable.img_chef_spl_fried_rice_fresh_1795010828210
        "chef_spl_chicken_fried_rice_solo" -> R.drawable.img_chef_spl_chicken_fried_rice_fresh_1795010775138
        "dal_fry_solo" -> R.drawable.img_dal_fry_clean
        "plain_dal_solo" -> R.drawable.img_plain_dal_fresh_1795010720424
        "dal_bukhara_solo" -> R.drawable.img_dal_bukhara_fresh_1795010721701
        "plain_tawa_roti_solo" -> R.drawable.img_plain_tawa_roti_distinct_1794967372678
        "tandoori_roti_solo" -> R.drawable.food_tandoori_roti_solo
        "tandoori_butter_roti_solo" -> R.drawable.food_tandoori_butter_roti_solo
        "missi_roti_solo" -> R.drawable.food_missi_roti_solo
        "desi_ghee_roti_solo" -> R.drawable.img_desi_ghee_roti_fresh_1795010747481
        "plain_naan_solo" -> R.drawable.food_plain_naan_solo
        "veg_stuff_naan_solo" -> R.drawable.img_veg_stuff_naan_fresh_1795010640169
        "kashmiri_naan_solo" -> R.drawable.img_kashmiri_naan_fresh_1795010405374
        "paneer_stuff_naan_solo" -> R.drawable.img_paneer_stuff_naan_fresh_1795010746257
        "aloo_paratha_solo" -> R.drawable.img_aloo_paratha_fresh_1795010638065
        "paneer_butter_paratha_solo" -> R.drawable.img_paneer_butter_paratha_fresh_1795010747978
        "gobi_paratha_solo" -> R.drawable.img_gobi_paratha_fresh_1795010638598
        "onion_masala_kulcha_solo" -> R.drawable.img_onion_masala_kulcha_fresh_1795010640628
        "paneer_masala_kulcha_solo" -> R.drawable.img_paneer_masala_kulcha_fresh_1795010747722
        "onion_cucumber_salad_solo" -> R.drawable.img_onion_cucumber_salad_fresh_1795089309556
        "fruit_mix_salad_solo" -> R.drawable.food_fruit_mix_salad_solo
        "cucumber_raita_solo" -> R.drawable.img_cucumber_raita_fresh_1794967195545
        "onion_raita_solo" -> R.drawable.img_onion_raita_fresh_1794967368683
        "mix_veg_raita_solo" -> R.drawable.food_mix_veg_raita_solo
        "boondi_raita_solo" -> R.drawable.img_boondi_raita_fresh_1795010404994
        "roasted_papad_solo" -> R.drawable.img_roasted_papad_fresh_1795096289435
        "fried_papad_solo" -> R.drawable.img_fried_papad_fresh_1795096259873
        "masala_papad_solo" -> R.drawable.img_masala_papad_fresh_1795096284273
        "mint_chutney_solo" -> R.drawable.food_mint_chutney_solo
        "onion_rings_chaat_solo" -> R.drawable.img_onion_rings_chaat_fresh_1795089240411
        "chef_seasoned_salad_solo" -> R.drawable.food_chef_seasoned_salad_solo
        "paneer_pizza_solo" -> R.drawable.img_paneer_pizza_golden_1794935564481
        "mushroom_pizza_solo" -> R.drawable.img_mushroom_pizza_golden_1794935565552
        else -> R.drawable.img_crush_special_golden_1794920794751
    }
}

object StoreContactHelper {

    fun dialStore(context: Context, phoneNumber: String = OFFICIAL_STORE_PHONE_DIAL) {
        try {
            val cleanDial = phoneNumber.replace(" ", "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanDial")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun buildOrderWhatsAppMessage(order: PizzaOrder): String {
        return buildString {
            appendLine("🍕 *MY CRUSH PIZZA – HAJIPUR ORDER* 🍕")
            appendLine("*Order ID:* ${order.orderNumber}")
            appendLine("*Customer:* ${order.customerName}")
            if (order.customerPhone.isNotBlank()) {
                appendLine("*Mobile:* ${order.customerPhone}")
            }
            appendLine("*Items:* ${order.itemsSummary}")
            appendLine("*Total Bill:* ${formatInr(order.totalInr)} (${order.paymentMethod})")
            appendLine("*Delivery Address:* ${order.deliveryAddress}")
            if (order.landmark.isNotBlank()) {
                appendLine("*Landmark:* ${order.landmark}")
            }
            appendLine("*Status:* ${order.orderStatus.title}")
            append("Please confirm my Hajipur pizza order!")
        }
    }

    fun openOrderOnWhatsApp(
        context: Context,
        order: PizzaOrder,
        whatsappDigits: String = OFFICIAL_STORE_WHATSAPP_NUMBER
    ) {
        openWhatsAppWithText(
            context = context,
            message = buildOrderWhatsAppMessage(order),
            whatsappDigits = whatsappDigits
        )
    }

    fun openWhatsAppWithText(
        context: Context,
        message: String,
        whatsappDigits: String = OFFICIAL_STORE_WHATSAPP_NUMBER
    ) {
        try {
            val cleanNumber = whatsappDigits.filter { it.isDigit() }.ifBlank { OFFICIAL_STORE_WHATSAPP_NUMBER }
            val encodedMessage = Uri.encode(message)
            val url = "https://wa.me/$cleanNumber?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openStoreLocationInGoogleMaps(
        context: Context,
        mapsUrl: String = HajipurLocations.STORE_MAPS_SHARE_URL
    ) {
        try {
            val cleanUrl = mapsUrl.trim().ifBlank { HajipurLocations.STORE_MAPS_SHARE_URL }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}

@Composable
fun MyCrushBrandBadge(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_my_crush_pizza_logo),
            contentDescription = "My Crush Pizza Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(if (compact) 38.dp else 46.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, CrushYellow, RoundedCornerShape(10.dp))
        )
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "MY CRUSH PIZZA",
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = CrushWhite,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = CrushYellow,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "HAJIPUR",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrushBlack,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "RN College Chautha Road, Hajipur",
                style = MaterialTheme.typography.bodySmall,
                color = CrushYellow
            )
        }
    }
}

@Composable
fun VegNonVegIndicator(isVeg: Boolean, modifier: Modifier = Modifier) {
    val tint = if (isVeg) VegGreenBright else CrushRed
    Box(
        modifier = modifier
            .size(16.dp)
            .border(1.5.dp, tint, RoundedCornerShape(3.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(tint)
        )
    }
}

/**
 * Prominent Store Direct Contact Card showing Call to Store (+91 95238 41563) and WhatsApp Order button.
 */
@Composable
fun StoreCallAndWhatsAppCard(
    order: PizzaOrder? = null,
    storePhone: String = OFFICIAL_STORE_PHONE_DISPLAY,
    headline: String = "Order Support • Call or WhatsApp Store",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("store_call_whatsapp_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CrushCardSurface),
        border = BorderStroke(1.5.dp, CrushYellow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CrushRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = "My Crush Pizza Store",
                        tint = CrushYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = CrushWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "My Crush Pizza – Hajipur • Store No: $storePhone",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrushYellow,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("store_official_phone_text")
                    )
                    if (order != null) {
                        Text(
                            text = "Tap below to send Order ${order.orderNumber} (${formatInr(order.totalInr)}) directly on WhatsApp or call the store.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrushMutedText
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Call Store Button
                Button(
                    onClick = { StoreContactHelper.dialStore(context, storePhone) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("call_store_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushYellow,
                        contentColor = CrushBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Call Store",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call Store",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // WhatsApp Order Button
                Button(
                    onClick = {
                        if (order != null) {
                            StoreContactHelper.openOrderOnWhatsApp(context, order)
                        } else {
                            StoreContactHelper.openWhatsAppWithText(
                                context = context,
                                message = "Hello My Crush Pizza – Hajipur ($storePhone)! I would like to place / check my pizza order."
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("whatsapp_store_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VegGreenBright,
                        contentColor = CrushBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "WhatsApp Store",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp Order",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            OutlinedButton(
                onClick = { StoreContactHelper.openStoreLocationInGoogleMaps(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_maps_link_btn"),
                border = BorderStroke(1.2.dp, CrushYellow.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Store Location on Google Maps",
                    tint = CrushYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Open Store Location on Google Maps",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrushYellow,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
