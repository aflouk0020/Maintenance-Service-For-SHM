package com.sigma.smarthome.maintenance_service.karate;

import com.intuit.karate.junit5.Karate;

class KarateRunner {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:karate").relativeTo(getClass());
    }
}