package com.mcmcourseproject.shopkeeper.main

class BackupCode {
    // Save ko lang dito yung mga lumang code

//    Add Item Firestore
//    val refStore = firestoreAuth.collection("storesTable").document(storeID)
//        .collection("inventory").document(itemBarcode)
//    refStore.get().addOnSuccessListener { item ->
//        if (item.exists()){
//            binding.itemCodeTextField.error = "The barcode is already been registered!"
//        } else if (!item.exists()){
//            val newItem = hashMapOf(
//                "itemID" to itemBarcode,
//                "itemName" to itemName,
//                "itemPrice" to itemPrice,
//                "itemQuantity" to itemQuantity
//            )
//            refStore.set(newItem)
//            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
//            finish()
//        }
//    }


//    Employee System Splash Screen
//    val storeID = document.getString("storeID")!!
//    val refStore = firestoreAuth.collection("storesTable").document(storeID)
//    refStore.get()
//        .addOnSuccessListener { documentStore ->
//            if (documentStore.exists()) { // Checks if the user is the owner or not
//                firestoreAuth.disableNetwork()
//                if (userID == documentStore.getString("storeOwner")) {
//                    val activity = Intent(this, MainMenuOwner::class.java)
//                    activity.putExtra("userID", userID)
//                    activity.putExtra("userEmail", userEmail)
//                    activity.putExtra("storeID", storeID)
//                    startActivity(activity)
//                    finish()
//                } else { // Employee Menu
//                    val activity = Intent(this, MainMenuEmployee::class.java)
//                    activity.putExtra("userID", userID)
//                    activity.putExtra("userEmail", userEmail)
//                    activity.putExtra("storeID", storeID)
//                    startActivity(activity)
//                    finish()
//                }
//            } else { // Creates new user personal database
//                val activity = Intent(this, WelcomeScreen::class.java)
//                activity.putExtra("userID", userID)
//                activity.putExtra("userEmail", userEmail)
//                startActivity(activity)
//                finish()
//            }
//        }
//        .addOnFailureListener { exception ->
//            Log.d("Failure", "get failed with ", exception)
//            startActivity(Intent(this, LoginActivity::class.java))
//        } just testing
}