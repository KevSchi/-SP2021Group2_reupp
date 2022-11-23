package campingplatz.captcha;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class CaptchaResponse {

    private Boolean success;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    public Boolean getSuccess() {
        return success;
    }

}