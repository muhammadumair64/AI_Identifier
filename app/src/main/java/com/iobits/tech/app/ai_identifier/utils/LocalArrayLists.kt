package com.iobits.tech.app.ai_identifier.utils

import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.database.dataClasses.MainRvDataClass
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager

object LocalArrayLists {


    fun animalsList(): ArrayList<MainRvDataClass>{
        val arrayList = ArrayList<MainRvDataClass>()

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_DOG_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.dog_img_main,
                    "Dogs",
                    "Identify\nYour\n" +
                            "Dog's\nBreed ",
                    R.color.dog_card_bg,
                    Constants.DOG,
                    it,
                    R.drawable.dog_card_arrow
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_CAT_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.cat_img_main,
                    "Cats",
                    "Identify\nYour\n" +
                            "Cat's\nBreed ",
                    R.color.cat_card_bg,
                    Constants.CAT,
                    it,
                    R.drawable.cat_card_arrow
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_INSECT_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.insect_img_main,
                    "Insects",
                    "Snap,\nAnalyze &\n" +
                            "Discover",
                    R.color.insects_card_bg,
                    Constants.INSECT,
                    it,
                    R.drawable.insect_card_arrow
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_BIRD_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.bird_img_main,
                    "Bird",
                    "Identify Any\nBird\n" +
                            "Instantly\nwith a Snap ",
                    R.color.bird_card_bg,
                    Constants.BIRD,
                    it,
                    R.drawable.bird_card_arrow
                )
            }?.let { arrayList.add(it) }

        return arrayList
    }

    fun objectsList(): ArrayList<MainRvDataClass>{
        val arrayList = ArrayList<MainRvDataClass>()

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_PLANT_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.plants_main_img,
                    "Plants",
                    "Snap & ID\nPlants Instantly",
                    R.color.cat_card_bg,
                    Constants.PLANT,
                    it,
                    R.drawable.cat_card_arrow
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_OBJECT_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.objects_main_img,
                    "Objects",
                    "Instantly Recognize and\n" +
                            "Classify Objects",
                    R.color.dog_card_bg,
                    Constants.OBJECT,
                    it,
                    R.drawable.dog_card_arrow
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_MUSHROOM_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.mushrooms_main_img,
                    "Mushrooms",
                    "Quickly Identify\n" +
                            "Species",
                    R.color.bird_card_bg,
                    Constants.MUSHROOM,
                    it,
                    R.drawable.bird_card_arrow
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_ROCK_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.stones_main_img,
                    "Rocks",
                    "Identify and Learn About\n" +
                            "Rock Types Instantly",
                    R.color.insects_card_bg,
                    Constants.ROCK,
                    it,
                    R.drawable.insect_card_arrow
                )
            }?.let { arrayList.add(it) }

        return arrayList
    }

    fun otherToolsList(): ArrayList<MainRvDataClass>{
        val arrayList = ArrayList<MainRvDataClass>()

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_COUNTRY_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.country_img_main,
                    "Predict Your\n" +
                            "Country of Flag",
                    "",
                    R.color.cat_card_bg,
                    Constants.ORIGIN,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_CELEBRITY_COUNT,1)
            ?.let {
                MainRvDataClass(
                    R.drawable.celebrity_img_main,
                    "Discover Celebrity\n" +
                            "with Ease",
                    "",
                    R.color.dog_card_bg,
                    Constants.CELEBRITY,
                    it
                )
            }?.let { arrayList.add(it) }


        return arrayList
    }


    fun mainRvDataArray(): ArrayList<MainRvDataClass>{
        val arrayList = ArrayList<MainRvDataClass>()

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_DOG_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.dog_id, "Identify Your Dog's Breed", "Quick and Accurate Breed Recognition at Your Fingertips", R.color.light_purple, Constants.DOG,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_PLANT_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.plant_id, "Smart Plant Identifier", "Snap, Scan, and Discover Plants Instantly", R.color.light_coral, Constants.PLANT,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_INSECT_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.insect_id, "Insect Identifier", "Snap, Analyze, and Discover", R.color.light_green, Constants.INSECT,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_CAT_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.cat_id, "Cat’s Breed Identifier", "Instant Species Identification at Your Fingertips", R.color.light_orange, Constants.CAT,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_OBJECT_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.object_id, "Object Identifier", "Instantly Recognize and Classify Objects", R.color.light_red, Constants.OBJECT,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_BIRD_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.birds_id, "Discover Birds Effortlessly", "Identify Any Bird Instantly with a Snap", R.color.light_blue, Constants.BIRD,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_MUSHROOM_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.mushroom_id, "Mushroom Identifier", "Quickly Identify Species", R.color.light_dim_orange, Constants.MUSHROOM,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_ROCK_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.rocks_id, "Discover Rocks Around You", "Identify and Learn About Rock Types Instantly", R.color.light_purple, Constants.ROCK,
                    it
                )
            }?.let { arrayList.add(it) }

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_COUNTRY_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.country_id, "Guess Your Country from the Flag", "Instantly Identify Your Country Based on Flags and Patterns", R.color.light_coral, Constants.ORIGIN,
                    it
                )
            }?.let { arrayList.add(it) }

//        arrayList.add(MainRvDataClass(R.drawable.dogs_voice, "Discover Your Dog's Voice", "Identify and Analyze Your Dog’s Unique Sounds", R.color.light_green, Constants.DOG_VOICE))
//        arrayList.add(MainRvDataClass(R.drawable.pet_translator, "Pet Translator", "Translate Your Pet's Sounds", R.color.light_orange, Constants.PET_TRANSLATOR))

        MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.ID_CELEBRITY_COUNT,1)
            ?.let {
                MainRvDataClass(R.drawable.celebrity_id, "Discover Celebrity with Ease", "Identify and Explore Your Favorite Stars Instantly", R.color.light_red, Constants.CELEBRITY,
                    it
                )
            }?.let { arrayList.add(it) }

        return arrayList
    }


}