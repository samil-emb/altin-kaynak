package com.example.altin_kaynak.data.model

data class SavingsTypeInfo(
    val label: String,
    val altinkaynakSymbol: String,
    val haremSymbol: String
)

val SAVINGS_TYPES = listOf(
    SavingsTypeInfo("Gram Altın", "PGA", "ALTIN"),
    SavingsTypeInfo("Çeyrek Altın", "PC", "CEYREK_YENI"),
    SavingsTypeInfo("Yarım Altın", "PY", "YARIM_YENI"),
    SavingsTypeInfo("Tam Altın", "PT", "TEK_YENI"),
    SavingsTypeInfo("Cumhuriyet Altını", "PA", "ATA_YENI"),
    SavingsTypeInfo("22 Ayar Bilezik (gram)", "PB", "AYAR22"),
    SavingsTypeInfo("14 Ayar Altın (gram)", "P14", "AYAR14")
)
