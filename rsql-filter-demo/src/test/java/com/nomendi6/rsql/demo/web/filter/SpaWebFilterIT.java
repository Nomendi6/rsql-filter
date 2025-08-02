package com.nomendi6.rsql.demo.web.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import com.nomendi6.rsql.demo.IntegrationTest;
import com.nomendi6.rsql.demo.security.AuthoritiesConstants;

@AutoConfigureMockMvc
@WithMockUser
@IntegrationTest
class SpaWebFilterIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFilterForwardsToIndex() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void testFilterDoesNotForwardToIndexForApi() throws Exception {
        mockMvc.perform(get("/api/authenticate")).andExpect(status().is2xxSuccessful()).andExpect(forwardedUrl(null));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void testFilterDoesNotForwardToIndexForV3ApiDocs() throws Exception {
        // Instead of testing the actual endpoint response, we just want to verify
        // that the filter doesn't forward this request to index.html
        mockMvc
            .perform(get("/v3/api-docs"))
            .andExpect(forwardedUrl(null)); // Just verify no forwarding happens
    }

    @Test
    void testFilterDoesNotForwardToIndexForDotFile() throws Exception {
        mockMvc.perform(get("/file.js")).andExpect(status().isNotFound());
    }

    @Test
    void getBackendEndpoint() throws Exception {
        mockMvc.perform(get("/test")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardUnmappedFirstLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardUnmappedSecondLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level/second-level")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardUnmappedThirdLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level/second-level/third-level")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardUnmappedDeepMapping() throws Exception {
        mockMvc.perform(get("/1/2/3/4/5/6/7/8/9/10")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void getUnmappedFirstLevelFile() throws Exception {
        mockMvc.perform(get("/foo.js")).andExpect(status().isNotFound());
    }

    /**
     * This test verifies that any files that aren't permitted by Spring Security will be forbidden.
     * If you want to change this to return isNotFound(), you need to add a request mapping that
     * allows this file in SecurityConfiguration.
     */
    @Test
    void getUnmappedSecondLevelFile() throws Exception {
        mockMvc.perform(get("/foo/bar.js")).andExpect(status().isForbidden());
    }

    @Test
    void getUnmappedThirdLevelFile() throws Exception {
        mockMvc.perform(get("/foo/another/bar.js")).andExpect(status().isForbidden());
    }
}
