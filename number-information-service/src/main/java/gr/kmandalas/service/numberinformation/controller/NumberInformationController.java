package gr.kmandalas.service.numberinformation.controller;

import gr.kmandalas.service.numberinformation.enums.MsisdnStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/number-information")
@RequiredArgsConstructor
public class NumberInformationController {

    @GetMapping
    public ResponseEntity<String> verifyMsisdn(@RequestParam String msisdn) {
        return ResponseEntity.ok().body(MsisdnStatus.OK.name());
    }

}
