def call(configOverrides) {

  def config = [
     releaseBranch: ['*/master'],
     autoVersionRelease: false 
  ]

  configOverrides.resolveStrategy = Closure.DELEGATE_FIRST
  configOverrides.delegate = config
  
  configOverrides()

  def branchName = scm.branches[0].name //env.BRANCH_NAME

  def isReleaseBranch = config.releaseBranch.contains(branchName)

  echo "std sbt called with config ${config} - ${isReleaseBranch} - ${branchName}"  
  
  try {
    node('master') {  
    	stage('checkout') {
    		checkout scm
    	}
      stage('Build') {
	      echo "Std Build"
      }
      stage('Test') {
        echo "Std Test + link" 	
        link() 
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

def link() {
   manager.createSummary("warning.gif").appendText("<h1>You have been warned!</h1>", false, false, false, "red") 
}