package api;

public class UrlHelper {
    public final static String domain = "juniperphoton.net";
    public static String UserCheckExist = "http://" + domain + "/schedule/User/CheckUserExist/v1?";
    public static String UserRegisterUri = "http://" + domain + "/schedule/User/Register/v1?";
    public static String UserLoginUri = "http://" + domain + "/schedule/User/Login/v1?";
    public static String UserGetSalt = "http://" + domain + "/schedule/User/GetSalt/v1";
    public static String ScheduleAddUri = "http://" + domain + "/schedule/Schedule/AddSchedule/v1?";
    public static String ScheduleUpdateUri = "http://" + domain + "/schedule/Schedule/UpdateContent/v1?";
    public static String ScheduleFinishUri = "http://" + domain + "/schedule/Schedule/FinishSchedule/v1?";
    public static String ScheduleDeleteUri = "http://" + domain + "/schedule/Schedule/DeleteSchedule/v1?";
    public static String ScheduleGetUri = "http://" + domain + "/schedule/Schedule/GetMySchedules/v1?";
    public static String ScheduleGetOrderUri = "http://" + domain + "/schedule/Schedule/GetMyOrder/v1?";
    public static String ScheduleSetOrderUri = "http://" + domain + "/schedule/Schedule/SetMyOrder/v1?";
    public static String UserGetCateUri = "http://" + domain + "/schedule/User/GetCateInfo/v1?";
    public static String UserUpdateCateUri = "http://" + domain + "/schedule/User/UpdateCateInfo/v1?";
}
