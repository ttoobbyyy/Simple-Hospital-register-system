package com.toby.hospitalRegister.util;

public class Constants {
    public static String NameTableDepartment = "department";
    public static String NameTableDoctor = "doctor";
    public static String NameTableCategoryRegister = "register_category";
    public static String NameTablePatient = "patient";
    public static String NameTableRegister = "register";

    public static String NameTableColumnDepartmentNumber = "depId";
    public static String NameTableColumnDepartmentName = "name";
    public static String NameTableColumnDepartmentPronounce = "py";

    public static String NameTableColumnDoctorNumber = "docId";
    public static String NameTableColumnDoctorDepartmentNumber = "depId";
    public static String NameTableColumnDoctorName = "name";
    public static String NameTableColumnDoctorPronounce = "py";
    public static String NameTableColumnDoctorPassword = "password";
    public static String NameTableColumnDoctorIsSpecialist = "specialList";
    public static String NameTableColumnDoctorLastLogin = "last_login_time";

    public static String NameTableColumnCategoryRegisterNumber = "catId";
    public static String NameTableColumnCategoryRegisterName = "name";
    public static String NameTableColumnCategoryRegisterPronounce = "py";
    public static String NameTableColumnCategoryRegisterDepartment = "depId";
    public static String NameTableColumnCategoryRegisterIsSpecialist = "specialList";
    public static String NameTableColumnCategoryRegisterMaxRegisterNumber = "max_reg_number";
    public static String NameTableColumnCategoryRegisterFee = "reg_fee";

    public static String NameTableColumnPatientNumber = "paId";
    public static String NameTableColumnPatientName = "name";
    public static String NameTableColumnPatientPassword = "password";
    public static String NameTableColumnPatientBalance = "balance";
    public static String NameTableColumnPatientLastLogin = "last_time_login";

    public static String NameTableColumnRegisterNumber = "regId";
    public static String NameTableColumnRegisterCategoryNumber = "catId";
    public static String NameTableColumnRegisterDoctorNumber = "docId";
    public static String NameTableColumnRegisterPatientNumber = "paId";
    public static String NameTableColumnRegisterCurrentRegisterCount = "current_reg_count";
    public static String NameTableColumnRegisterUnregister = "unreg";
    public static String NameTableColumnRegisterFee = "reg_fee";
    public static String NameTableColumnRegisterDateTime = "reg_datetime";

}
