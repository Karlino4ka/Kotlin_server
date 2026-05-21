package com.example.kotlin_kursach_server

class InstitutionRepository {

    private val institutions = listOf(
        Institution(
            id = "1",
            name = "Московский государственный университет",
            type = InstitutionType.UNIVERSITY,
            city = "Москва",
            address = "ул. Ленинские Горы, 1",
            description = "Ведущий классический университет России.",
            phone = "+7 (495) 939-10-00",
            website = "https://www.msu.ru",
        ),
        Institution(
            id = "2",
            name = "Санкт-Петербургский политехнический университет",
            type = InstitutionType.UNIVERSITY,
            city = "Санкт-Петербург",
            address = "Политехническая ул., 29",
            description = "Технический университет с инженерными и IT-направлениями.",
            phone = "+7 (812) 534-11-11",
            website = "https://www.spbstu.ru",
        ),
        Institution(
            id = "3",
            name = "Колледж информационных технологий",
            type = InstitutionType.COLLEGE,
            city = "Казань",
            address = "ул. Баумана, 15",
            description = "Среднее профессиональное образование в сфере IT.",
            phone = "+7 (843) 200-00-00",
        ),
        Institution(
            id = "4",
            name = "Лицей № 153",
            type = InstitutionType.SCHOOL,
            city = "Москва",
            address = "ул. Вавилова, 57",
            description = "Профильный лицей с углублённым изучением математики и физики.",
            phone = "+7 (495) 123-45-67",
        ),
        Institution(
            id = "5",
            name = "Новосибирский государственный университет",
            type = InstitutionType.UNIVERSITY,
            city = "Новосибирск",
            address = "ул. Пирогова, 1",
            description = "Крупный научно-образовательный центр Сибири.",
            website = "https://www.nsu.ru",
        ),
    )

    fun getAll(): List<Institution> = institutions

    fun getById(id: String): Institution? = institutions.find { it.id == id }
}
