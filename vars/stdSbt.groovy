def call(configOverrides) {

  def config = [
     releaseBranch: ['master'],
     autoVersionRelease: false 
  ]

  configOverrides.resolveStrategy = Closure.DELEGATE_FIRST
  configOverrides.delegate = config
  
  configOverrides()


  def isReleaseBranch = config.releaseBranch.contains(env.BRANCH_NAME)

  ansiColor('xterm') {
  	echo "std sbt called with config ${config}"  
  }
  
  try {
    node('master') {  
    	stage('checkout') {
    		checkout scm
    	}
      stage('Build') {
	    echo "Std Build"
      }
      stage('Test') {
        echo "Std Test"	
      }
      if (isReleaseBranch) {
      	if (!config.autoVersionRelease) {
      		stage("Confirm Release") {
      			milestone()
      			input(id: "Confirmation", message: "Confirm Release")
      			milestone()
      		}
      	}
      	stage("Release") {
            echo "Auto Released"
        }

      } else {
      	stage("Publish") {
		  echo "Std Publish"	
      	}
      }
      stage("Clean") {
		echo "Std Clean"	
      }
    }
  } catch(err) {
  	echo "error - cleaning up ${err}"
  	stage("Clean") {
	  echo "Std Clean After error"	
    }
  } 
}