package org.sormas.e2etests.helpers.api;

import static org.sormas.e2etests.constants.api.Endpoints.FACILITIES;

import io.restassured.http.Method;
import javax.inject.Inject;
import org.sormas.e2etests.helpers.RestAssuredClient;
import org.sormas.e2etests.pojo.api.Request;

public class FacilityHelper {

  private final RestAssuredClient restAssuredClient;

  @Inject
  public FacilityHelper(RestAssuredClient restAssuredClient) {
    this.restAssuredClient = restAssuredClient;
  }

  public void getFacilitiesByRegion(String specificPath, String regionUuid, Integer since) {
    restAssuredClient.sendRequest(
        Request.builder()
            .method(Method.GET)
            .path(FACILITIES + specificPath + regionUuid + "/" + since)
            .build());
  }
}
