package com.rayray.madcapstoneproject.model

/***
 * @author Raymond Chang
 *
 * This DepartmentEnum prevent typos
 */
enum class DepartmentEnum(val department: String) {
    ENTERTAINMENT("Entertainment"),
    PERSONAL_CARE("Verzorging"),
    HOUSEHOLD_GOODS("Klein huishoudelijk"),
    KITCHEN_APPLIANCES("Witgoed"),
    TV_HIFI("TV & Hifi"),
    TELECOM("Telecom"),
    COMPUTER("Computer")
}