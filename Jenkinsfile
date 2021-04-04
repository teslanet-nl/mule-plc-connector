pipeline
{
    environment
    {
        MVN_SETTINGS = credentials( 'secret-teslanet-maven-settings.xml' )
    }
    agent
    { 
        dockerfile
        {
            args '--network sonar_network'
        }
    }
    stages
    {
        stage('build')
        {
            steps
            {
                sh 'mvn -B -s $MVN_SETTINGS clean package -Psonar'
            }
        }
        stage('test')
        {
            steps
            {
                sh 'mvn -B -s $MVN_SETTINGS test -Psonar'
            }
        }
        stage('install')
        {
            environment
            {
                GNUPGHOME = '/var/jenkins_home/.gnupg/'
            }   
            steps
            {
               sh 'mvn -B -s $MVN_SETTINGS install -Psonar'
            }
        }
        stage('scan')
        {
            environment
            {
                GNUPGHOME = '/var/jenkins_home/.gnupg/'
            }   
            steps
            {
               sh 'mvn -B -s $MVN_SETTINGS sonar:sonar -Psonar'
            }
        }
    }
}