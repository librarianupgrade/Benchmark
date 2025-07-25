/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

def con, s;
try {
    def props = new Properties();
    props.setProperty("user", config_user)
    props.setProperty("password", config_password)
    con = new org.mariadb.jdbc.Driver().connect("jdbc:mariadb://${config_host}:${config_port_mariadb}?useSSL=false&allowPublicKeyRetrieval=true", props)
    s = con.createStatement();
    s.execute("DROP DATABASE IF EXISTS `${config_dbname}`")
    s.execute("CREATE DATABASE `${config_dbname}`")
} finally {
    s?.close();
    con?.close();
}

println "Prepared empty database `${config_dbname}`"

// create directories under target to silence out liquibase plugin
new File( testBasedir, "target/classes" ).mkdirs()
new File( testBasedir, "target/test-classes" ).mkdirs()

def sourceChangelog = new File( basedir, 'allChanges/test-changelog.xml' )
def targetChangelog = new File( testBasedir, 'test-changelog.xml' )
targetChangelog.text = sourceChangelog.text
println "Copied ${sourceChangelog} to ${targetChangelog}"

def sourceProperties = new File( basedir, 'allChanges/test.properties' )
def targetProperties = new File( testBasedir, 'test.properties' )
targetProperties.text = sourceProperties.text
println "Copied ${sourceProperties} to ${targetProperties}"

return true
