package com.rpc.test;

import com.rpc.annotation.Service;
import com.rpc.api.ByeService;

@Service
public class ByeServiceImpl implements ByeService {
    @Override
    public String bye(String name) {
        return "bye, " + name;
    }
}
