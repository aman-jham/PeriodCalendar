//package com.period.calendarview;
//
//import android.content.Context;
//
//@SuppressWarnings("unused")
//public final class TrunkBranchAnnals {
//
//    private static String[] TRUNK_STR = null;
//
//    private static String[] BRANCH_STR = null;
//
//    public static void init(Context context) {
//        if (TRUNK_STR != null) {
//            return;
//        }
//        TRUNK_STR = context.getResources().getStringArray(R.array.trunk_string_array);
//        BRANCH_STR = context.getResources().getStringArray(R.array.branch_string_array);
//
//    }
//
//    @SuppressWarnings("all")
//    public static String getTrunkString(int year) {
//        return TRUNK_STR[getTrunkInt(year)];
//    }
//
//    @SuppressWarnings("all")
//    public static int getTrunkInt(int year) {
//        int trunk = year % 10;
//        return trunk == 0 ? 9 : trunk - 1;
//    }
//
//    @SuppressWarnings("all")
//    public static String getBranchString(int year) {
//        return BRANCH_STR[getBranchInt(year)];
//    }
//
//    @SuppressWarnings("all")
//    public static int getBranchInt(int year) {
//        int branch = year % 12;
//        return branch == 0 ? 11 : branch - 1;
//    }
//
//    public static String getTrunkBranchYear(int year) {
//        return String.format("%s%s", getTrunkString(year), getBranchString(year));
//    }
//}
