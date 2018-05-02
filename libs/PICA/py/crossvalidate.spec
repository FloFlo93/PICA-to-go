# -*- mode: python -*-

block_cipher = None


a = Analysis(['crossvalidate.py'],
             pathex=['/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/Libs/PICA-develop'],
             binaries=[],
             datas=[],
             hiddenimports=["pica.trainers.libsvm.libSVMTrainer", "pica.classifiers.libsvm.libSVMClassifier"],
             hookspath=[],
             runtime_hooks=[],
             excludes=[],
             win_no_prefer_redirects=False,
             win_private_assemblies=False,
             cipher=block_cipher)
pyz = PYZ(a.pure, a.zipped_data,
             cipher=block_cipher)
exe = EXE(pyz,
          a.scripts,
          a.binaries,
          a.zipfiles,
          a.datas,
          name='crossvalidate',
          debug=False,
          strip=False,
          upx=True,
          runtime_tmpdir=None,
          console=True )
