package com.kyad.traystorage.app;

import com.kyad.traystorage.R;

public class Constants {
    public static String TERM_TYPE = "term_type";
    public static String F_NUM = "f_num";
    public static String F_TYPE = "f_type";
    public static String PAYMENT_NUM = "payment_num";
    public static String PAYMENT_TYPE = "payment_TYPE";
    public static String PAYMENT_PRICE = "payment_price";
    public static String SCHEDULE_NUM = "schedule_num";
    public static String REVIEW_URL = "https://m.traystorage/echo?m=a";
    public static String CONTACT_URL = "https://m.traystorage/echo?m=a";
    public static String CLIENT_CENTER_PHONE = "0570-0267-7000";
    public static String adminEmailAddress = "admin@traystorage.com";
    public static String FIREBASE_DYNAMIC_LINK = "https://traystorage.page.link";

    public enum InputError {
        idle,
        ok,
        id,
        pwd,
        pwd_confirm,
        cert_num,
        name,
        phone
    }

    public enum MainTab {
        list(0),
        map(1),
        search(2);

        public int value;

        MainTab(int value) {
            this.value = value;
        }
    }

    public enum SubTab {
        stadium(0),
        match(1);

        public int value;

        SubTab(int value) {
            this.value = value;
        }
    }

    public enum TimeOption {
        all(0),
        am(1),
        pm(2),
        night(3);

        public int value;

        TimeOption(int value) {
            this.value = value;
        }
    }

    public enum SizeOption {
        all(0),
        three(1),
        four(2),
        five(3),
        six(4);

        public int value;

        SizeOption(int value) {
            this.value = value;
        }
    }

    public enum SexOption {
        all(0),
        male(1),
        female(2);

        public int value;

        SexOption(int value) {
            this.value = value;
        }
    }

    public enum DoorOption {
        all(0),
        indoor(1),
        outdoor(2);

        public int value;

        DoorOption(int value) {
            this.value = value;
        }
    }

    public enum TermType {
        faq(1),
        term_use(2),
        privacy(3),
        third_party(4),
        refund(5),
        vaccination(6);

        public int value;

        TermType(int value) {
            this.value = value;
        }
    }

    public enum TermTypeURL {
        faq_url("https://m.traystorage/echo?m=a"),
        term_use_url("https://m.traystorage/echo?m=a"),
        privacy_url("https://m.traystorage/echo?m=a"),
        third_party_url("https://m.traystorage/echo?m=a"),
        refund_url("https://m.traystorage/echo?m=a"),
        vaccination_url("https://m.traystorage/echo?m=a");

        public String value;

        TermTypeURL(String value) {
            this.value = value;
        }
    }

    public enum DetailTab {
        detailGuide(0),
        useRule(1),
        refundRule(2),
        notice(3);

        public int value;

        DetailTab(int value) {
            this.value = value;
        }
    }

    public enum InfoTab {
        detail(0),
        rule(1),
        refund(2),
        notice(3);

        public int value;

        InfoTab(int value) {
            this.value = value;
        }
    }

    public enum TimeCase {
        all(1440),
        am(360),
        pm(360),
        night(480),
        am_pm(720),
        am_night(720),
        pm_night(840);

        public int value;

        TimeCase(int value) {
            this.value = value;
        }
    }

    public static String allTimeString = "6#12#18#24#6";
    public static String amTimeString = "6#7 30#9#10 30#12";
    public static String pmTimeString = "12#13 30#15#16 30#18";
    public static String nightTimeString = "18#20#22#24#2";
    public static String amPmTimeString = "6#9#12#15#18";
    public static String pmNightTimeString = "12#15 30#19#22 30#2";

    public enum StartTimeCase {
        all("06:00"),
        am("06:00"),
        pm("12:00"),
        night("18:00"),
        am_pm("06:00"),
        am_night("06:00"),
        pm_night("12:00");

        public String value;

        StartTimeCase(String value) {
            this.value = value;
        }
    }

    public enum ManagerTab {
        attendance(0),
        team(1),
        skill(2),
        manner(3);

        public int value;

        ManagerTab(int value) {
            this.value = value;
        }
    }

    public enum ColorIndex {
        first(1),
        second(2),
        third(3),
        fourth(4);

        public int value;

        ColorIndex(int value) {
            this.value = value;
        }
    }

    public enum LabelColorArray {
        color0(R.color.labelcolor1),
        color1(R.color.labelcolor2),
        color2(R.color.labelcolor3),
        color3(R.color.labelcolor4),
        color4(R.color.labelcolor5),
        color5(R.color.labelcolor6),
        color6(R.color.labelcolor7),
        color7(R.color.labelcolor8),
        color8(R.color.labelcolor9),
        color9(R.color.labelcolor10);

        public int value;

        LabelColorArray(int value) { this.value = value; }
    };

}
