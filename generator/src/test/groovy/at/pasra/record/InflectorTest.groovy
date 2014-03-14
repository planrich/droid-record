package at.pasra.record

import at.pasra.record.util.Inflector

/**
 * Created by rich on 3/14/14.
 */
class InflectorTest extends GroovyTestCase {

    public void testCamelize() {
        assertEquals("Test", Inflector.camelize("test"))
        assertEquals("Test", Inflector.camelize(Inflector.tabelize("test")))
    }

    public void testJavaClass() {

        assertEquals("CatAndDog", Inflector.javaClassName("cat_and_dog"))
        assertEquals("CatAndDogs", Inflector.pluralizeCamel(Inflector.camelize("cat_and_dog")))
        assertEquals("CatAndDogs", Inflector.javaClassName(Inflector.pluralize("cat_and_dog")))
    }
}
