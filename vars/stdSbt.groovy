def call(configOverrides) {

  def config = [
     releaseBranch: ['*/master','master'],
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

  echo "ENV -> "
  sh 'env > env.txt' 
  for (String i : readFile('env.txt').split("\r?\n")) {
    println i
  }
  echo "params -> "
  for (String i : params) {
    println i
  }
  echo "currentBuild.buildVariables -> "
  for (String i : currentBuild.buildVariables) {
    println i
  }
  

      }
      stage('Test') {
        echo "Std Test" 	
      }
      

      if (isReleaseBranch) {
      	if (!config.autoVersionRelease) {
      		stage("Confirm Release") {
              echo "mkdir"
              sh 'mkdir -p target/releaselink'
              echo "write file"
              def currentVersion = 'v1.0.0'
              def htmlString = '<a href="https://github.com/andersbohn/jdemoprj/releases/new?tag='+currentVersion+'">Rel-' + env.BRANCH_NAME + '</a>'              
              addHtmlBadge html: htmlString, id: 'releaselink'

            def proceedWithRelease = true
            try { 
      			  milestone()
              link()
      			  //input(id: "Confirmation", message: "Confirm Release")
      			  milestone()
            } catch(e) {
              proceedWithRelease = false
            }
            if (proceedWithRelease) {
              stage("Release") {
                echo "Released by confirmation"
              }
            }
      		}
      	} else {
      	  stage("Release") {
            echo "Auto Released"
          }
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

def binding = getBinding()
def manager = binding.getVariable("manager")

def link() {
  input(id: 'Confirmation', message: 'Add tag in git then confirm Release')
  //manager.createSummary("warning.gif").appendText("<h1>You have been warned!</h1>", false, false, false, "red") 
}

