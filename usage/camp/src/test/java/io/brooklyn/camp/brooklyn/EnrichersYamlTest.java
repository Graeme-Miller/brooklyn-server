package io.brooklyn.camp.brooklyn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import brooklyn.entity.Entity;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.EntityInternal;
import brooklyn.policy.Enricher;
import brooklyn.test.Asserts;
import brooklyn.test.entity.TestEntity;
import brooklyn.test.policy.TestEnricher;
import brooklyn.util.collections.MutableMap;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@Test
public class EnrichersYamlTest extends AbstractYamlTest {
    private static final Logger log = LoggerFactory.getLogger(EnrichersYamlTest.class);

    @Test
    public void testWithAppEnricher() throws Exception {
        Entity app = createAndStartApplication("test-app-with-enricher.yaml");
        waitForApplicationTasks(app);
        Assert.assertEquals(app.getDisplayName(), "test-app-with-enricher");
        
        log.info("App started:");
        Entities.dumpInfo(app);
        
        Assert.assertEquals(app.getEnrichers().size(), 1);
        final Enricher enricher = app.getEnrichers().iterator().next();
        Assert.assertTrue(enricher instanceof TestEnricher);
        Assert.assertEquals(enricher.getConfig(TestEnricher.CONF_NAME), "Name from YAML");
        Assert.assertEquals(enricher.getConfig(TestEnricher.CONF_FROM_FUNCTION), "$brooklyn: is a fun place");
        
        Entity target = ((EntityInternal)app).getExecutionContext().submit(MutableMap.of(), new Callable<Entity>() {
            public Entity call() {
                return enricher.getConfig(TestEnricher.TARGET_ENTITY);
            }}).get();
        Assert.assertNotNull(target);
        Assert.assertEquals(target.getDisplayName(), "testentity");
        Assert.assertEquals(target, app.getChildren().iterator().next());
        Entity targetFromFlag = ((EntityInternal)app).getExecutionContext().submit(MutableMap.of(), new Callable<Entity>() {
            public Entity call() {
                return enricher.getConfig(TestEnricher.TARGET_ENTITY_FROM_FLAG);
            }}).get();
        Assert.assertEquals(targetFromFlag, target);
        Map<?, ?> leftoverProperties = ((TestEnricher) enricher).getLeftoverProperties();
        Assert.assertEquals(leftoverProperties.get("enricherLiteralValue1"), "Hello");
        Assert.assertEquals(leftoverProperties.get("enricherLiteralValue2"), "World");
        Assert.assertEquals(leftoverProperties.size(), 2);
    }
    
    @Test
    public void testWithEntityEnricher() throws Exception {
        final Entity app = createAndStartApplication("test-entity-with-enricher.yaml");
        waitForApplicationTasks(app);
        Assert.assertEquals(app.getDisplayName(), "test-entity-with-enricher");

        log.info("App started:");
        Entities.dumpInfo(app);

        Assert.assertEquals(app.getEnrichers().size(), 0);
        Assert.assertEquals(app.getChildren().size(), 1);
        final Entity child = app.getChildren().iterator().next();
        Asserts.eventually(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return child.getEnrichers().size();
            }
        }, Predicates.<Integer> equalTo(1));        
        Enricher enricher = child.getEnrichers().iterator().next();
        Assert.assertNotNull(enricher);
        Assert.assertTrue(enricher instanceof TestEnricher, "enricher=" + enricher + "; type=" + enricher.getClass());
        Assert.assertEquals(enricher.getConfig(TestEnricher.CONF_NAME), "Name from YAML");
        Assert.assertEquals(enricher.getConfig(TestEnricher.CONF_FROM_FUNCTION), "$brooklyn: is a fun place");
        
        Assert.assertEquals(((TestEnricher) enricher).getLeftoverProperties(),
                ImmutableMap.of("enricherLiteralValue1", "Hello", "enricherLiteralValue2", "World"));
    }
    
    @Test
    public void testPropagatingEnricher() throws Exception {
        Entity app = createAndStartApplication("test-propagating-enricher.yaml");
        waitForApplicationTasks(app);
        Assert.assertEquals(app.getDisplayName(), "test-propagating-enricher");

        log.info("App started:");
        Entities.dumpInfo(app);
        TestEntity entity = (TestEntity)app.getChildren().iterator().next();
        entity.setAttribute(TestEntity.NAME, "New Name");
        Asserts.eventually(Entities.attributeSupplier(app, TestEntity.NAME), Predicates.<String>equalTo("New Name"));
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
}