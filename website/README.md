under 'website' directory:

npm install

gulp clean

gulp build

./scripts/javadocs.sh 

hugo

hugo server --watch --ignoreCache


website/public is a submodule pointing to gh-pages branch.
one time setup: 
rm -rf public
git submodule add https://github.iu.edu/IU-Big-Data-Lab/Harp.git public
git submodule update --init
cd public
git checkout gh-pages
git remote rename origin upstream
